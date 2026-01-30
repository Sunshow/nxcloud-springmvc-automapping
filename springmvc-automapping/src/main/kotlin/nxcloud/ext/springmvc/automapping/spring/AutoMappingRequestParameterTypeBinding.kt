package nxcloud.ext.springmvc.automapping.spring

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRegistration
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestParameterTypeResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.MethodParameter
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * 请求参数类型的绑定关系维护
 */
open class AutoMappingRequestParameterTypeBinding {

    private val logger = KotlinLogging.logger {}

    @Autowired(required = false)
    private var requestParameterTypeResolvers: List<AutoMappingRequestParameterTypeResolver>? = null

    // 原始的处理方法和对应方法参数之间的映射关系
    private val originalBindingCache: MutableMap<Method, Array<Class<*>>> = ConcurrentHashMap()

    // 经过处理转换之后的处理方法和对应方法参数之间的映射关系, 只保存了经过 AutoMappingRequestParameterTypeResolver 处理后的方法, 数据量少于原始映射 originalBindingCache
    private val bindingCache: MutableMap<Method, Array<Class<*>>> = ConcurrentHashMap()

    // 声明处类型缓存, 缓存声明接口或者Bean的类型, 便于后续找到原始的注解信息
    private val declaringMethodCache: MutableMap<Method, Method> = ConcurrentHashMap()

    // 缓存映射路径中包含的路径参数, 实现类似 Spring MVC 的 @PathVariable 的功能
    // Method 对应的 Map<String, Set<String>> 中的 String 为映射路径的 pattern, Set<String> 为 pattern 中包含的路径参数
    private val pathVariableCache: MutableMap<Method, Map<String, Set<String>>> = ConcurrentHashMap()

    /**
     * 注册绑定关系
     *
     * @param registration 注册信息
     */
    fun registerBinding(registration: AutoMappingRegistration) {
        val method = registration.method
        val declaredMethod = registration.declaringMethod
        val mapping = registration.mapping

        if (bindingCache.containsKey(method)) {
            return
        }

        requestParameterTypeResolvers
            ?.firstOrNull {
                it.isSupported(registration)
            }
            ?.apply {
                bindingCache[method] = resolveParameterType(registration)
                // 原始的也要存下来供后续判断映射时候的原始参数类型使用
                originalBindingCache[method] = method.parameterTypes
            }
            ?: run {
                originalBindingCache[method] = method.parameterTypes
            }

        // 如果未指定声明处的方法, 直接使用最后处理的方法
        declaringMethodCache[method] = declaredMethod ?: method

        // 解析 Path 中的路径参数
        mapping
            .also {
                resolvePathParameters(declaringMethodCache[method]!!, it)
                    ?.apply {
                        if (isNotEmpty()) {
                            if (this.size > 1) {
                                throw IllegalStateException("Method ${declaringMethodCache[method]!!.name} has more than one path pattern, which is not supported")
                            }
                            pathVariableCache[method] = if (pathVariableCache.containsKey(method)) {
                                pathVariableCache[method]!! + this
                            } else {
                                this
                            }
                        }
                    }
            }
    }

    private fun resolvePathParameters(method: Method, mapping: RequestMappingInfo): Map<String, Set<String>>? {
        // 支持带约束的路径变量, 如 {id:\d+} 提取变量名 id
        val regex = """\{([^:}]+)(?::[^}]*)?}""".toRegex()
        val patterns = mapping.pathPatternsCondition?.patterns ?: return null
        return patterns
            .map {
                it.patternString
            }
            .associateWith { p ->
                regex.findAll(p)
                    .map {
                        it.groupValues[1]
                    }
                    .toSet()
            }
            .filter {
                it.value.isNotEmpty()
            }
    }

    // 调用此方法时需要确保此方法一定是已经自动注册了的, 即原始映射缓存中一定存在
    fun resolveBinding(parameter: MethodParameter): Class<*> {
        val method = getBridgedMethod(parameter)
        // 如果原始映射有, 而转换后的映射里也有, 则使用转换后对应位置的映射, 否则使用原始映射
        val originalParameterTypes = originalBindingCache[method]!!
        val parameterTypes = bindingCache[method]
            ?: return originalParameterTypes[parameter.parameterIndex]

        return parameterTypes[parameter.parameterIndex]
    }

    private fun <T : Annotation> getAnnotation(method: Method, annotationType: Class<T>): T? {
        return this.getAnnotation(method, annotationType, false)
    }

    private fun <T : Annotation> getAnnotation(
        method: Method,
        annotationType: Class<T>,
        searchClassType: Boolean
    ): T? {
        if (!isSupportedMethod(method)) {
            return null
        }
        val declaringMethod = declaringMethodCache[method]!!
        var annotation = AnnotationUtils.findAnnotation(declaringMethod, annotationType)
        if (annotation == null && searchClassType) {
            annotation = AnnotationUtils.findAnnotation(declaringMethod.declaringClass, annotationType)
        }
        return annotation
    }

    fun <T : Annotation> getAnnotation(handlerMethod: HandlerMethod, annotationType: Class<T>): T? {
        return this.getAnnotation(handlerMethod, annotationType, false)
    }

    fun <T : Annotation> getAnnotation(
        handlerMethod: HandlerMethod,
        annotationType: Class<T>,
        searchClassType: Boolean
    ): T? {
        return getAnnotation(handlerMethod.method, annotationType, searchClassType)
    }

    fun <T : Annotation> getMethodAnnotation(parameter: MethodParameter, annotationType: Class<T>): T? {
        return this.getMethodAnnotation(parameter, annotationType, false)
    }

    fun <T : Annotation> getMethodAnnotation(
        parameter: MethodParameter,
        annotationType: Class<T>,
        searchClassType: Boolean
    ): T? {
        val method = getBridgedMethod(parameter)
        return getAnnotation(method, annotationType, searchClassType)
    }

    private fun isSupportedMethod(method: Method): Boolean {
        return bindingCache[method] != null || originalBindingCache[method] != null
    }

    fun isSupported(parameter: MethodParameter): Boolean {
        val method = getBridgedMethod(parameter)
        return bindingCache[method] != null || originalBindingCache[method] != null
    }

    private fun getBridgedMethod(parameter: MethodParameter): Method {
        // 兼容 springframework 6.1.9 之后的改动, 通过 this$0 获取 HandlerMethod, 直接获取 method 会得到抽象代理类的方法
        val handlerMethod = parameter::class.java
            .getDeclaredField("this$0")
            .apply {
                trySetAccessible()
            }
            .get(parameter) as HandlerMethod
        return handlerMethod.method
    }

    fun isSupportedParameterType(parameter: MethodParameter): Boolean {
        // val method = parameter.method!!
        val method = getBridgedMethod(parameter)
        if (!isSupportedMethod(method)) {
            return false
        }

        val parameterType = parameter.parameterType

        // 如果是 Path 参数, 需要支持处理
        if (isSupportedPathVariable(parameter)) {
            return true
        }

        return resolveBinding(parameter)
            .let {
                !parameterType.isPrimitive
                        && !parameterType.canonicalName.startsWith("java.")
                        && !HttpServletRequest::class.java.isAssignableFrom(parameterType)
                        && !HttpServletResponse::class.java.isAssignableFrom(parameterType)
                        && !HttpSession::class.java.isAssignableFrom(parameterType)
                        && !Model::class.java.isAssignableFrom(parameterType)
                        && !ModelMap::class.java.isAssignableFrom(parameterType)
            }
    }

    fun isSupportedPathVariable(parameter: MethodParameter): Boolean {
        // 对于例如像执行方法是由父类代理增强出来的情况, 参数名获取不到, 这种情况不做支持, 实际业务一般不需要关心
        if (parameter.parameterName == null) {
            return false
        }
        return pathVariableCache[parameter.method!!]
            ?.flatMap {
                it.value
            }
            ?.contains(parameter.parameterName!!) == true
    }

    fun isSupportedPathVariable(parameter: MethodParameter, pattern: String): Boolean {
        val method = getBridgedMethod(parameter)
        if (!isSupportedMethod(method)) {
            return false
        }

        // 对于例如像执行方法是由父类代理增强出来的情况, 参数名获取不到, 这种情况不做支持, 实际业务一般不需要关心
        if (parameter.parameterName == null) {
            return false
        }

        return pathVariableCache[method]
            ?.get(pattern)
            ?.contains(parameter.parameterName!!) == true
    }

    fun getPathVariableNames(method: Method, pattern: String): Set<String>? {
        return pathVariableCache[method]?.get(pattern)
    }
}