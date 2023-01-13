package nxcloud.ext.springmvc.automapping.spring

import com.fasterxml.jackson.databind.ObjectMapper
import nxcloud.ext.springmvc.automapping.context.AutoMappingContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


class AutoMappingReturnValueWebMvcConfigurer : WebMvcConfigurer {

    @Autowired
    private lateinit var autoMappingContext: AutoMappingContext

    @Autowired
    private lateinit var converters: List<HttpMessageConverter<*>>

    // TODO 提供扩展点适配不同的 JSON 库
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    override fun addReturnValueHandlers(handlers: MutableList<HandlerMethodReturnValueHandler>) {
        // 如果有需要直接返回 void 的情况，需要通过直接设置 RequestMappingHandlerAdapter 的 returnValueHandlers,
        // 把 AutoMappingRequestResponseBodyMethodProcessor 放在 ViewNameMethodReturnValueHandler 前面, 否则会被 ViewNameMethodReturnValueHandler 拦截
        // 绝大部分情况接口都会做统一封装, 所以不会有直接返回 void 的情况, 所以这里暂时不做处理
        handlers.add(
            AutoMappingRequestResponseBodyMethodProcessor(
                autoMappingContext,
                converters
            )
        )
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(
            AutoMappingRequestBodyArgumentResolver(
                autoMappingContext,
                objectMapper,
            )
        )
    }

}