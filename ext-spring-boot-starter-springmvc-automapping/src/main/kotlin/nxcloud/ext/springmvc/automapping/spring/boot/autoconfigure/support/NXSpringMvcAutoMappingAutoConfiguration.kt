package nxcloud.ext.springmvc.automapping.spring.boot.autoconfigure.support

import com.fasterxml.jackson.databind.ObjectMapper
import nxcloud.ext.springmvc.automapping.spi.impl.FormUrlencodedAutoMappingRequestParameterResolver
import nxcloud.ext.springmvc.automapping.spi.impl.GetAutoMappingRequestParameterResolver
import nxcloud.ext.springmvc.automapping.spi.impl.JacksonAutoMappingRequestParameterResolver
import nxcloud.ext.springmvc.automapping.spring.AutoMappingHandlerMethodArgumentResolver
import nxcloud.ext.springmvc.automapping.spring.AutoMappingRequestHandlerRegistrar
import nxcloud.ext.springmvc.automapping.spring.AutoMappingRequestParameterTypeBinding
import nxcloud.ext.springmvc.automapping.spring.AutoMappingRequestResponseBodyMethodProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice

@AutoConfiguration
@ConditionalOnClass(RequestMappingHandlerMapping::class)
class NXSpringMvcAutoMappingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AutoMappingRequestHandlerRegistrar::class)
    protected fun autoMappingRequestHandlerRegistrar(): AutoMappingRequestHandlerRegistrar {
        return AutoMappingRequestHandlerRegistrar()
    }

    @Bean
    @ConditionalOnMissingBean(AutoMappingHandlerMethodArgumentResolver::class)
    protected fun autoMappingRequestBodyArgumentResolver(): AutoMappingHandlerMethodArgumentResolver {
        return AutoMappingHandlerMethodArgumentResolver()
    }

    @Bean
    @ConditionalOnMissingBean(AutoMappingRequestResponseBodyMethodProcessor::class)
    protected fun autoMappingRequestResponseBodyMethodProcessor(
        converters: List<HttpMessageConverter<*>>,
        responseBodyAdvices: List<ResponseBodyAdvice<out Any>>?,
    ): AutoMappingRequestResponseBodyMethodProcessor {
        return AutoMappingRequestResponseBodyMethodProcessor(converters, responseBodyAdvices)
    }

    @Bean
    @ConditionalOnMissingBean(AutoMappingRequestParameterTypeBinding::class)
    protected fun autoMappingRequestParameterTypeBinding(): AutoMappingRequestParameterTypeBinding {
        return AutoMappingRequestParameterTypeBinding()
    }

    @Bean
    @ConditionalOnClass(ObjectMapper::class)
    @ConditionalOnMissingBean(JacksonAutoMappingRequestParameterResolver::class)
    protected fun jacksonAutoMappingRequestParameterResolver(objectMapper: ObjectMapper): JacksonAutoMappingRequestParameterResolver {
        return JacksonAutoMappingRequestParameterResolver(objectMapper)
    }

    @Bean
    @ConditionalOnMissingBean(FormUrlencodedAutoMappingRequestParameterResolver::class)
    protected fun formUrlencodedAutoMappingRequestParameterResolver(): FormUrlencodedAutoMappingRequestParameterResolver {
        return FormUrlencodedAutoMappingRequestParameterResolver()
    }

    @Bean
    @ConditionalOnMissingBean(GetAutoMappingRequestParameterResolver::class)
    protected fun getAutoMappingRequestParameterResolver(): GetAutoMappingRequestParameterResolver {
        return GetAutoMappingRequestParameterResolver()
    }

    @Configuration
    @ConditionalOnClass(WebMvcConfigurer::class)
    internal class AutoMappingReturnValueWebMvcConfigurer : WebMvcConfigurer {

        @Autowired
        private lateinit var autoMappingHandlerMethodArgumentResolver: AutoMappingHandlerMethodArgumentResolver

        @Autowired
        private lateinit var autoMappingRequestResponseBodyMethodProcessor: AutoMappingRequestResponseBodyMethodProcessor

        override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
            // 如果有需要直接返回 void 的情况，需要通过直接设置 RequestMappingHandlerAdapter 的 returnValueHandlers,
            // 把 AutoMappingRequestResponseBodyMethodProcessor 放在 ViewNameMethodReturnValueHandler 前面, 否则会被 ViewNameMethodReturnValueHandler 拦截
            // 绝大部分情况接口都会做统一封装, 所以不会有直接返回 void 的情况, 所以这里暂时不做处理
            handlers.add(
                autoMappingRequestResponseBodyMethodProcessor
            )
        }

        override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
            resolvers.add(
                autoMappingHandlerMethodArgumentResolver
            )
        }

    }

}