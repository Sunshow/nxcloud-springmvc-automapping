package nxcloud.ext.springmvc.automapping.spring

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.MethodParameter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice


class AutoMappingRequestResponseBodyMethodProcessor(
    converters: List<HttpMessageConverter<*>>,
    responseBodyAdvices: List<ResponseBodyAdvice<out Any>>?,
) : RequestResponseBodyMethodProcessor(converters, responseBodyAdvices) {

    @Autowired
    private lateinit var autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding

    override fun supportsReturnType(returnType: MethodParameter): Boolean {
        // 处理所有自动映射托管的方法
        return autoMappingRequestParameterTypeBinding.isSupported(returnType)
    }

    override fun handleReturnValue(
        returnValue: Any?,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer,
        webRequest: NativeWebRequest
    ) {
        super.handleReturnValue(returnValue, returnType, mavContainer, webRequest)
    }
}