package nxcloud.ext.springmvc.automapping.spi.impl

import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import javax.servlet.http.HttpServletRequest

class FormUrlencodedAutoMappingRequestParameterResolver : AbstractQueryParameterAutoMappingRequestParameterResolver() {

    override fun isSupported(
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest
    ): Boolean {
        val servletRequest = webRequest.getNativeRequest(HttpServletRequest::class.java)!!

        return servletRequest.contentType?.startsWith("application/x-www-form-urlencoded") ?: false
    }

}