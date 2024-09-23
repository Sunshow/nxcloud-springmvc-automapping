package nxcloud.ext.springmvc.automapping.sample

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nxcloud.ext.springmvc.automapping.spring.AutoMappingRequestParameterTypeBinding
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.AsyncHandlerInterceptor

@Component
class SampleSessionScopeInterceptor(
    private val autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding
) : AsyncHandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {
            println(
                autoMappingRequestParameterTypeBinding.getAnnotation(
                    handler,
                    SampleSessionRequired::class.java,
                    true,
                )
            )
        }
        return true
    }
}