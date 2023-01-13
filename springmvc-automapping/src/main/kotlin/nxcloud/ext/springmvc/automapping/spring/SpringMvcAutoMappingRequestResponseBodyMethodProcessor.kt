package nxcloud.ext.springmvc.automapping.spring

import nxcloud.ext.springmvc.automapping.context.AutoMappingContext
import org.springframework.core.MethodParameter
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor


class SpringMvcAutoMappingRequestResponseBodyMethodProcessor(
    private val autoMappingContext: AutoMappingContext,
    converters: List<HttpMessageConverter<*>>
) : RequestResponseBodyMethodProcessor(converters) {

    override fun supportsReturnType(returnType: MethodParameter): Boolean {
        return autoMappingContext.isSupported(returnType.containingClass)
    }

}