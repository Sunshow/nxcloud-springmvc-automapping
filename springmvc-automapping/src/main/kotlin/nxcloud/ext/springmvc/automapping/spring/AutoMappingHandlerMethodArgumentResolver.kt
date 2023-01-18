package nxcloud.ext.springmvc.automapping.spring

import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestParameterInjector
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestParameterResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer


class AutoMappingHandlerMethodArgumentResolver : HandlerMethodArgumentResolver {

    @Autowired(required = false)
    private var autoMappingRequestParameterResolvers: List<AutoMappingRequestParameterResolver>? = null

    @Autowired(required = false)
    private var autoMappingRequestParameterInjectors: List<AutoMappingRequestParameterInjector>? = null

    @Autowired
    private lateinit var autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding


    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.method
            ?.let {
                autoMappingRequestParameterTypeBinding.isSupportedParameterType(parameter)
            }
            ?: false
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        // 拿到实际的类型
        val parameterType =
            autoMappingRequestParameterTypeBinding.resolveBinding(parameter.method!!, parameter)
                ?: return null

        // 从请求中解析出参数并构建相应的对象
        val parameterObj = autoMappingRequestParameterResolvers
            ?.firstOrNull {
                it.isSupported(parameter, parameterType, webRequest)
            }
            ?.resolveParameter(parameter, parameterType, webRequest)
            ?: return null

        // 注入参数
        autoMappingRequestParameterInjectors
            ?.filter {
                it.isSupported(parameterObj, parameter, parameterType, webRequest)
            }
            ?.onEach {
                it.inject(parameterObj)
            }

        return parameterObj
    }


}