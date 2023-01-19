package nxcloud.ext.springmvc.automapping.sample

import nxcloud.ext.springmvc.automapping.spring.AutoMappingRequestParameterTypeBinding
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.AsyncHandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class SampleSessionScopeInterceptor(
    private val autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding
) : AsyncHandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {
            println(
                autoMappingRequestParameterTypeBinding.getAnnotation(
                    handler.method,
                    SampleSessionRequired::class.java,
                    true,
                )
            )
        }
        return true
    }
}