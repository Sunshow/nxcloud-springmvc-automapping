package nxcloud.ext.springmvc.automapping.sample

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class SampleWebConfig : WebMvcConfigurer {

    @Autowired
    private lateinit var sampleSessionScopeInterceptor: SampleSessionScopeInterceptor

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(sampleSessionScopeInterceptor)
            .addPathPatterns("/user/**")
    }

}