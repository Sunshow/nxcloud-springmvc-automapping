package nxcloud.ext.springmvc.automapping.sample

import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestResolver
import nxcloud.ext.springmvc.automapping.spi.RequestResolvedInfo
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration
import org.springframework.web.util.pattern.PathPatternParser


@Component
class ContractAutoMappingRequestResolver : AutoMappingRequestResolver {

    override fun resolveMapping(bean: Any, beanName: String): List<RequestResolvedInfo> {
        val options = BuilderConfiguration()
        options.patternParser = PathPatternParser()

        return listOf(
            RequestResolvedInfo(
                RequestMappingInfo
                    .paths("/rename")
                    .consumes(MediaType.APPLICATION_JSON_VALUE)
                    .methods(RequestMethod.POST)
                    .options(options)
                    .build(),
                bean,
                bean.javaClass.getMethod("rename", User::class.java)
            ),
            RequestResolvedInfo(
                RequestMappingInfo
                    .paths("/info")
                    .consumes(MediaType.APPLICATION_JSON_VALUE)
                    .methods(RequestMethod.POST)
                    .options(options)
                    .build(),
                bean,
                bean.javaClass.getMethod("info")
            ),
            RequestResolvedInfo(
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

    override fun isSupportedMapping(bean: Any, beanName: String): Boolean {
        return true
    }
    
}