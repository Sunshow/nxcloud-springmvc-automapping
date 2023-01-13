package nxcloud.ext.springmvc.automapping.sample

import nxcloud.ext.springmvc.automapping.handler.AutoMappingBeanHandler
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo

@Component
class ContractAutoMappingBeanHandler : AutoMappingBeanHandler {

    override fun mapping(bean: Any, beanName: String): List<RequestMappingInfo> {
        return listOf(
            RequestMappingInfo
                .paths("/auto")
                .consumes(MediaType.APPLICATION_JSON_VALUE)
                .methods(RequestMethod.POST)
                .build()
        )
    }

}