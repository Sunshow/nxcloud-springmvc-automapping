package nxcloud.ext.springmvc.automapping.spring

import com.fasterxml.jackson.databind.ObjectMapper
import nxcloud.ext.springmvc.automapping.context.AutoMappingContext
import org.springframework.core.MethodParameter
import org.springframework.util.StreamUtils
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.io.IOException
import javax.servlet.http.HttpServletRequest


class AutoMappingRequestBodyArgumentResolver(
    private val autoMappingContext: AutoMappingContext,
    private val objectMapper: ObjectMapper,
) : HandlerMethodArgumentResolver {

    private val attrJsonRequestBody = "JSON_REQUEST_BODY"

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return autoMappingContext.isSupported(parameter.containingClass)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val body = getRequestBody(webRequest)

        return objectMapper.readValue(body, parameter.parameterType)
    }

    private fun getRequestBody(webRequest: NativeWebRequest): String {
        val servletRequest = webRequest.getNativeRequest(HttpServletRequest::class.java)!!
        return servletRequest.getAttribute(attrJsonRequestBody) as String?
            ?: try {
                val jsonBody = StreamUtils.copyToString(servletRequest.inputStream, Charsets.UTF_8)
                servletRequest.setAttribute(attrJsonRequestBody, jsonBody)
                jsonBody
            } catch (e: IOException) {
                throw RuntimeException("读取 RequestBody 出错", e)
            }
    }
}