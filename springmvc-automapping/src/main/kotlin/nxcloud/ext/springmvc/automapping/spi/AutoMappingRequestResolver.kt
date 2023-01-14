package nxcloud.ext.springmvc.automapping.spi

import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import java.lang.reflect.Method

interface AutoMappingRequestResolver {

    /**
     * 解析要自动映射的 Bean 并生成对应的映射信息
     */
    fun mapping(bean: Any, beanName: String): List<RequestResolvedInfo>

}

open class RequestResolvedInfo(
    val mapping: RequestMappingInfo,
    val bean: Any,
    val method: Method,
)