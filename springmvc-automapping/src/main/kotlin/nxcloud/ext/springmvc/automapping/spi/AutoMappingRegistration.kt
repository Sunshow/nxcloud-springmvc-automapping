package nxcloud.ext.springmvc.automapping.spi

import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import java.lang.reflect.Method

open class AutoMappingRegistration(
    val mapping: RequestMappingInfo,
    val bean: Any,
    val method: Method,
)