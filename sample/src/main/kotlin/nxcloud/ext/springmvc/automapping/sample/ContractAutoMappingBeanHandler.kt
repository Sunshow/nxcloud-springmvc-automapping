package nxcloud.ext.springmvc.automapping.sample

import nxcloud.ext.springmvc.automapping.handler.AutoMappingBeanHandler
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration
import org.springframework.web.util.pattern.PathPatternParser




@Component
class ContractAutoMappingBeanHandler : AutoMappingBeanHandler {

    override fun mapping(bean: Any, beanName: String): List<RequestMappingInfo> {
        val options = BuilderConfiguration()
        options.patternParser = PathPatternParser()

        return listOf(
            RequestMappingInfo
                .paths("/auto")
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .methods(RequestMethod.POST)
                .options(options)
                .build()
        )
    }

}