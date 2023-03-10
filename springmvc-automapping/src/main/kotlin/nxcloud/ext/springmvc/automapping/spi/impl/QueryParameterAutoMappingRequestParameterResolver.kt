package nxcloud.ext.springmvc.automapping.spi.impl

import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.spi.AutoMappingFieldArgumentResolver
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestParameterResolver
import nxcloud.ext.springmvc.automapping.spring.AutoMappingRequestParameterTypeBinding
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Lazy
import org.springframework.core.MethodParameter
import org.springframework.core.convert.ConversionService
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.servlet.HandlerMapping
import javax.servlet.http.HttpServletRequest

class QueryParameterAutoMappingRequestParameterResolver : AutoMappingRequestParameterResolver {

    private val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Lazy
    @Autowired
    private lateinit var conversionService: ConversionService

    @Autowired
    private lateinit var autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding

    @Autowired(required = false)
    private var fieldArgumentResolverList: List<AutoMappingFieldArgumentResolver>? = null

    override fun isSupported(
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest
    ): Boolean {
        val servletRequest = webRequest.getNativeRequest(HttpServletRequest::class.java)!!

        return servletRequest.method.uppercase() == "GET"
                || (
                servletRequest.method.uppercase() == "POST"
                        && (
                        servletRequest.contentType.isNullOrBlank()
                                || servletRequest.contentType!!.startsWith("application/x-www-form-urlencoded")
                        )
                )
    }

    override fun resolveParameter(
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest
    ): Any? {
        val servletRequest = webRequest.getNativeRequest(HttpServletRequest::class.java)!!

        // 默认调用空构造函数创建实例
        val parameterObj = resolvedParameterType.getDeclaredConstructor().newInstance()

        // 填充属性
        val parameters = servletRequest.parameterMap.toMutableMap()
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
                    parameters[key] = arrayOf(uriTemplateVars[key]!!)
                }
            }

        resolvedParameterType.declaredFields
            .onEach { field ->
                val fieldName = field.name
                if (parameters.containsKey(fieldName)) {
                    val value = parameters[fieldName] ?: return@onEach
                    // 请求参数有值 则填充
                    field.isAccessible = true

                    // 检查是否是数组
                    // TODO 暂未处理集合类的情况
                    if (field.type.isArray) {
                        val array = value.map { conversionService.convert(it, field.type.componentType) }.toTypedArray()
                        field.set(parameterObj, array)
                    } else {
                        field.set(parameterObj, conversionService.convert(value[0], field.type))
                    }
                } else {
                    // 请求参数中未包含同名属性, 尝试是否自定义解析
                    fieldArgumentResolverList
                        ?.firstOrNull {
                            it.supportsParameter(field, parameterObj, parameter, resolvedParameterType, webRequest)
                        }
                        ?.resolveArgument(field, parameterObj, parameter, resolvedParameterType, webRequest)
                        ?.apply {
                            field.isAccessible = true
                            field.set(parameterObj, this)
                        }
                }
            }

        return parameterObj
    }

}