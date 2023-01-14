package nxcloud.ext.springmvc.automapping.spi

import org.springframework.core.MethodParameter
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import java.lang.reflect.Method

interface AutoMappingRequestResolver {

    /**
     * 解析要自动映射的 Bean 并生成对应的映射信息
     */
    fun resolveMapping(bean: Any, beanName: String): List<RequestResolvedInfo>

    /**
     * 用于将处理方法的入参解析为实际的请求参数类型, 例如将抽象类型参数处理成具体的类型
     */
    fun resolveParameterClass(parameter: MethodParameter): Class<*> {
        return parameter.parameterType
    }

    /**
     * 是否支持映射
     */
    fun isSupportedMapping(bean: Any, beanName: String): Boolean {
        return false
    }

    /**
     * 是否支持参数解析
     */
    fun isSupportedParameterClass(parameter: MethodParameter): Boolean {
        return false
    }

}

open class RequestResolvedInfo(
    val mapping: RequestMappingInfo,
    val bean: Any,
    val method: Method,
)