package nxcloud.ext.springmvc.automapping.sample

import com.fasterxml.jackson.databind.ObjectMapper
import nxcloud.ext.springmvc.automapping.spring.AutoMappingRequestParameterTypeBinding
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@RestControllerAdvice
class SampleResponseBodyWrapperAdvice(
    private val autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding,
    private val objectMapper: ObjectMapper,
) : ResponseBodyAdvice<Any> {
    override fun supports(returnType: MethodParameter, converterType: Class<out HttpMessageConverter<*>>): Boolean {
        return returnType.method
            ?.let {
                autoMappingRequestParameterTypeBinding.isSupported(returnType)
            }
            ?: false
    }

    override fun beforeBodyWrite(
        body: Any?,
        returnType: MethodParameter,
        selectedContentType: MediaType,
        selectedConverterType: Class<out HttpMessageConverter<*>>,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Any? {
        // 防止重复包裹的问题出现
        if (body is SampleResponseBodyWrapper) {
            return body
        }
        val wrapper = SampleResponseBodyWrapper(data = body)
        return if (body is String) {
            // 如果返回是字符串在这里直接序列化好, 否则会转换错误
            objectMapper.writeValueAsString(wrapper)
        } else {
            wrapper
        }
    }

    data class SampleResponseBodyWrapper(
        val code: Int = 0,
        val message: String = "成功",
        val data: Any? = null,
    )
}