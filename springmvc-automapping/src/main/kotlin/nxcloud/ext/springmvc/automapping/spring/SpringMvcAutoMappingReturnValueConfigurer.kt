package nxcloud.ext.springmvc.automapping.spring

import nxcloud.ext.springmvc.automapping.context.AutoMappingContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


class SpringMvcAutoMappingReturnValueConfigurer : WebMvcConfigurer {

    @Autowired
    private lateinit var autoMappingContext: AutoMappingContext

    @Autowired
    private lateinit var converters: List<HttpMessageConverter<*>>

    override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        handlers.add(
            SpringMvcAutoMappingRequestResponseBodyMethodProcessor(
                autoMappingContext,
                converters
            )
        )
    }
}