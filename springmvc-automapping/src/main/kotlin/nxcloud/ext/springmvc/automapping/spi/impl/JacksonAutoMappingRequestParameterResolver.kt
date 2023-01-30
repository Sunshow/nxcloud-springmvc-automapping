package nxcloud.ext.springmvc.automapping.spi.impl

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestParameterResolver
import nxcloud.ext.springmvc.automapping.spring.AutoMappingRequestParameterTypeBinding
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.core.MethodParameter
import org.springframework.core.convert.ConversionService
import org.springframework.util.StreamUtils
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.servlet.HandlerMapping
import java.io.IOException
import javax.servlet.http.HttpServletRequest

class JacksonAutoMappingRequestParameterResolver(
    private val objectMapper: ObjectMapper,
) : AutoMappingRequestParameterResolver {

    private val logger = KotlinLogging.logger {}

    private val attrJsonRequestBody = "JSON_REQUEST_BODY"

    @Lazy
    @Autowired
    private lateinit var conversionService: ConversionService

    @Autowired
    private lateinit var autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding

    override fun resolveParameter(
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest
    ): Any? {
        val body = getRequestBody(webRequest)

        val parameterObj = objectMapper.readValue(body, resolvedParameterType)

        // 填充属性
        val parameters = mutableMapOf<String, String>()
        // 如果有对象中的部分参数被设置成路径参数需要一块填充
        val pattern = webRequest.getAttribute(
            HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE,
            RequestAttributes.SCOPE_REQUEST
        ) as String
        autoMappingRequestParameterTypeBinding.getPathVariableNames(parameter.method!!, pattern)
            ?.also {
                @Suppress("UNCHECKED_CAST")
                val uriTemplateVars = webRequest.getAttribute(
                    HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST
                ) as Map<String, String>
                // 合并到需要被填充的属性
                it.forEach { key ->
                    parameters[key] = uriTemplateVars[key]!!
                }
            }

        parameters.forEach { (key, value) ->
            try {
                val field = resolvedParameterType.getDeclaredField(key)
                field.isAccessible = true
                field.set(parameterObj, conversionService.convert(value, field.type))
            } catch (e: NoSuchFieldException) {
                // 忽略传递未知属性的情况
                logger.debug { "No such field: $key" }
            }
        }

        return parameterObj
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