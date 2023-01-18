package nxcloud.ext.springmvc.automapping.spi

import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest

/**
 * 解析实际请求时传入的请求参数并转换为需要的类型实例
 */
interface AutoMappingRequestParameterResolver {

    fun resolveParameter(
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest,
    ): Any?

    /**
     * 是否支持参数解析
     */
    fun isSupported(
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest,
    ): Boolean {
        return false
    }

}