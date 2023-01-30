package nxcloud.ext.springmvc.automapping.spring

import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestParameterTypeResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.MethodParameter
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import java.lang.reflect.Method
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

/**
 * 请求参数类型的绑定关系维护
 */
open class AutoMappingRequestParameterTypeBinding {

    private val logger = KotlinLogging.logger {}

    @Autowired(required = false)
    private var requestParameterTypeResolvers: List<AutoMappingRequestParameterTypeResolver>? = null

    // 原始的映射关系
    private val originalBindingCache: MutableMap<Method, Array<Class<*>>> = mutableMapOf()

    // 经过处理之后的映射关系
    private val bindingCache: MutableMap<Method, Array<Class<*>>> = mutableMapOf()

    // 声明处类型缓存, 缓存声明接口或者Bean的类型, 便于后续找到原始的注解信息
    private val declaringMethodCache: MutableMap<Method, Method> = mutableMapOf()

    // 缓存映射路径中包含的路径参数, 实现类似 Spring MVC 的 @PathVariable 的功能
    // Method 对应的 Map<String, Set<String>> 中的 String 为映射路径的 pattern, Set<String> 为 pattern 中包含的路径参数
    private val pathVariableCache: MutableMap<Method, Map<String, Set<String>>> = mutableMapOf()

    fun registerBinding(method: Method, declaredMethod: Method?, mapping: RequestMappingInfo?) {
        if (bindingCache.containsKey(method)) {
            return
        }

        requestParameterTypeResolvers
            ?.firstOrNull {
                it.isSupported(method)
            }
            ?.apply {
                bindingCache[method] = resolveParameterType(method)
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
            ?.also {
                resolvePathParameters(declaringMethodCache[method]!!, it)
                    ?.apply {
                        if (isNotEmpty()) {
                            if (this.size > 1) {
                                throw IllegalStateException("Method ${declaringMethodCache[method]!!.name} has more than one path pattern, which is not supported")
                            }
                            pathVariableCache[method] = this
                        }
                    }
            }
    }

    private fun resolvePathParameters(method: Method, mapping: RequestMappingInfo): Map<String, Set<String>>? {
        val regex = """\{(.*?)}""".toRegex()
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
    fun resolveBinding(method: Method, parameter: MethodParameter): Class<*>? {
        // 如果原始映射有, 而转换后的映射里也有, 则使用转换后对应位置的映射, 否则使用原始映射
        val originalParameterTypes = originalBindingCache[method]!!
        val parameterTypes = bindingCache[method]
            ?: return originalParameterTypes[parameter.parameterIndex]

        return parameterTypes[parameter.parameterIndex]
    }

    fun <T : Annotation> getAnnotation(method: Method, annotationType: Class<T>): T? {
        return this.getAnnotation(method, annotationType, false)
    }

    fun <T : Annotation> getAnnotation(
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

    fun isSupportedMethod(method: Method): Boolean {
        return bindingCache[method] != null || originalBindingCache[method] != null
    }

    fun isSupportedParameterType(parameter: MethodParameter): Boolean {
        val method = parameter.method!!
        if (!isSupportedMethod(method)) {
            return false
        }

        val parameterType = parameter.parameterType

        // 如果是 Path 参数, 需要支持处理
        if (isSupportedPathVariable(parameter)) {
            return true
        }

        return resolveBinding(method, parameter)
            ?.let {
                !parameterType.isPrimitive
                        && !parameterType.canonicalName.startsWith("java.")
                        && !HttpServletRequest::class.java.isAssignableFrom(parameterType)
                        && !HttpServletResponse::class.java.isAssignableFrom(parameterType)
                        && !HttpSession::class.java.isAssignableFrom(parameterType)
                        && !Model::class.java.isAssignableFrom(parameterType)
                        && !ModelMap::class.java.isAssignableFrom(parameterType)
            }
            ?: false
    }

    fun isSupportedPathVariable(parameter: MethodParameter): Boolean {
        return pathVariableCache[parameter.method!!]
            ?.flatMap {
                it.value
            }
            ?.contains(parameter.parameterName!!) == true
    }

    fun isSupportedPathVariable(parameter: MethodParameter, pattern: String): Boolean {
        val method = parameter.method!!
        if (!isSupportedMethod(method)) {
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