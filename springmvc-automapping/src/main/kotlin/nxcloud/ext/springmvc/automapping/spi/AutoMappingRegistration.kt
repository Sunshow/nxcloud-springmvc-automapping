package nxcloud.ext.springmvc.automapping.spi

import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import java.lang.reflect.Method

open class AutoMappingRegistration(
    // 声明处的方法
    val declaringMethod: Method? = null,
    val mapping: RequestMappingInfo,
    // 响应处理的Bean类型
    val beanType: Class<*>,
    val bean: Any,
    val method: Method,
)