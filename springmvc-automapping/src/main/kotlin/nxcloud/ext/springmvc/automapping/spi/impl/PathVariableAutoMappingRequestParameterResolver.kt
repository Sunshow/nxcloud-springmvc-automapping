package nxcloud.ext.springmvc.automapping.spi.impl

import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestParameterResolver
import nxcloud.ext.springmvc.automapping.spring.AutoMappingRequestParameterTypeBinding
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.core.MethodParameter
import org.springframework.core.convert.ConversionService
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.servlet.HandlerMapping

class PathVariableAutoMappingRequestParameterResolver : AutoMappingRequestParameterResolver {

    private val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding

    @Lazy
    @Autowired
    private lateinit var conversionService: ConversionService

    override fun isSupported(
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest
    ): Boolean {
        val pattern = webRequest.getAttribute(
            HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE,
            RequestAttributes.SCOPE_REQUEST
        ) as String

        // 按照映射路径匹配
        return autoMappingRequestParameterTypeBinding.isSupportedPathVariable(parameter, pattern)
    }

    override fun resolveParameter(
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest
    ): Any {
        @Suppress("UNCHECKED_CAST")
        val uriTemplateVars = webRequest.getAttribute(
            HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST
        ) as Map<String, String>

        return uriTemplateVars[parameter.parameterName!!]
            ?.let {
                conversionService.convert(it, resolvedParameterType)
            }
            ?: throw IllegalArgumentException("Missing path variable: ${parameter.parameterName}")
    }
}