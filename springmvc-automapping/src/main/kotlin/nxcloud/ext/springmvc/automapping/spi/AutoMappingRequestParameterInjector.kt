package nxcloud.ext.springmvc.automapping.spi

import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest

/**
 * 注入上下文中的属性到需要的方法中, 例如通常在拦截器中处理的属性如 userId 等
 */
interface AutoMappingRequestParameterInjector {

    /**
     * 注入属性
     */
    fun inject(
        parameterObj: Any,
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest,
    ) {
    }

    fun isSupported(
        parameterObj: Any,
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest,
    ): Boolean {
        return false
    }

}
