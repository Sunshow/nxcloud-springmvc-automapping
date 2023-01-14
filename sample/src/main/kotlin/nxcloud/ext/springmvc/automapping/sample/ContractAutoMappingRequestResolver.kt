package nxcloud.ext.springmvc.automapping.sample

import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestResolver
import nxcloud.ext.springmvc.automapping.spi.RequestHandlerInfo
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration
import org.springframework.web.util.pattern.PathPatternParser


@Component
class ContractAutoMappingRequestResolver : AutoMappingRequestResolver {

    override fun mapping(bean: Any, beanName: String): List<RequestHandlerInfo> {
        val options = BuilderConfiguration()
        options.patternParser = PathPatternParser()

        return listOf(
            RequestHandlerInfo(
                RequestMappingInfo
                    .paths("/rename")
                    .consumes(MediaType.APPLICATION_JSON_VALUE)
                    .methods(RequestMethod.POST)
                    .options(options)
                    .build(),
                bean,
                bean.javaClass.getMethod("rename", User::class.java)
            ),
            RequestHandlerInfo(
                RequestMappingInfo
                    .paths("/info")
                    .consumes(MediaType.APPLICATION_JSON_VALUE)
                    .methods(RequestMethod.POST)
                    .options(options)
                    .build(),
                bean,
                bean.javaClass.getMethod("info")
            ),
            RequestHandlerInfo(
                RequestMappingInfo
                    .paths("/submit")
                    .consumes(MediaType.APPLICATION_JSON_VALUE)
                    .methods(RequestMethod.POST)
                    .options(options)
                    .build(),
                bean,
                bean.javaClass.getMethod("submit", User::class.java)
            ),
        )
    }

}