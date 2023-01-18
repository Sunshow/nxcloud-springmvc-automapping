package nxcloud.ext.springmvc.automapping.spi.impl

import com.fasterxml.jackson.databind.ObjectMapper
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestParameterResolver
import org.springframework.core.MethodParameter
import org.springframework.util.StreamUtils
import org.springframework.web.context.request.NativeWebRequest
import java.io.IOException
import javax.servlet.http.HttpServletRequest

class JacksonAutoMappingRequestParameterResolver(
    private val objectMapper: ObjectMapper,
) : AutoMappingRequestParameterResolver {

    private val attrJsonRequestBody = "JSON_REQUEST_BODY"

    override fun resolveParameter(
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest
    ): Any? {
        val body = getRequestBody(webRequest)

        return objectMapper.readValue(body, resolvedParameterType)
    }

    override fun isSupported(
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest
    ): Boolean {
        val servletRequest = webRequest.getNativeRequest(HttpServletRequest::class.java)!!

        return servletRequest.contentType?.startsWith("application/json") ?: false
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