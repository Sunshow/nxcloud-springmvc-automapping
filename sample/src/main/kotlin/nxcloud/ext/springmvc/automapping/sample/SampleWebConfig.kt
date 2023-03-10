package nxcloud.ext.springmvc.automapping.sample

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import javax.servlet.http.HttpServletRequest

@Configuration
class SampleWebConfig : WebMvcConfigurer {

    @Autowired
    private lateinit var sampleSessionScopeInterceptor: SampleSessionScopeInterceptor

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(sampleSessionScopeInterceptor)
            .addPathPatterns("/user/**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(SearchDomainArgumentResolver())
    }
}

class SearchDomainArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.parameterType.isAssignableFrom(SearchDomain::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any {
        val servletRequest = webRequest.getNativeRequest(HttpServletRequest::class.java)!!

        val name = servletRequest.getParameter("name")
        val age = servletRequest.getParameter("age")?.toInt() ?: 0
        val page = servletRequest.getParameter("page")?.toInt() ?: 1

        return SearchDomain(User(name, age), page)
    }

}