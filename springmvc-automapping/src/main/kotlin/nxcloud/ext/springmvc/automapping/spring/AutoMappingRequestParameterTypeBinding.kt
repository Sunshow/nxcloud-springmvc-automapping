package nxcloud.ext.springmvc.automapping.spring

import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestParameterTypeResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ui.Model
import org.springframework.ui.ModelMap
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

    private val bindingCache: MutableMap<Method, Array<Class<*>>> = mutableMapOf()

    fun registerBinding(method: Method) {
        if (bindingCache.containsKey(method)) {
            return
        }

        requestParameterTypeResolvers
            ?.firstOrNull {
                it.isSupported(method)
            }
            ?.apply {
                bindingCache[method] = resolveParameterType(method)
            }
            ?: run {
                bindingCache[method] = method.parameterTypes
            }
    }

    fun resolveBinding(method: Method, parameterType: Class<*>): Class<*>? {
        val parameterTypes = bindingCache[method] ?: method.parameterTypes
        return parameterTypes
            .firstOrNull {
                it.isAssignableFrom(parameterType)
            }
    }

    fun isSupportedMethod(method: Method): Boolean {
        return bindingCache[method] != null
    }

    fun isSupportedParameterType(method: Method, parameterType: Class<*>): Boolean {
        if (!isSupportedMethod(method)) {
            return false
        }

        return resolveBinding(method, parameterType)
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
}