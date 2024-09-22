package nxcloud.ext.springmvc.automapping.sample

import nxcloud.ext.springmvc.automapping.spi.AutoMappingBeanRequestResolver
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRegistration
import org.springframework.aop.support.AopUtils
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.RequestMappingInfo.BuilderConfiguration
import org.springframework.web.util.pattern.PathPatternParser


@Component
class SampleAutoMappingBeanRequestResolver : AutoMappingBeanRequestResolver {

    override fun resolveMapping(bean: Any, beanName: String): List<AutoMappingRegistration> {
        val options = BuilderConfiguration()
        options.patternParser = PathPatternParser()

        val beanType = AopUtils.getTargetClass(bean)

        return listOf(
            AutoMappingRegistration(
                null,
                RequestMappingInfo
                    .paths("/rename")
                    .consumes(MediaType.APPLICATION_JSON_VALUE)
                    .methods(RequestMethod.POST)
                    .options(options)
                    .build(),
                beanType = User::class.java,
                bean,
                bean.javaClass.getMethod("rename", User::class.java)
            ),
            AutoMappingRegistration(
                null,
                RequestMappingInfo
                    .paths("/info")
                    .consumes(MediaType.APPLICATION_JSON_VALUE)
                    .methods(RequestMethod.POST)
                    .options(options)
                    .build(),
                beanType = User::class.java,
                bean,
                bean.javaClass.getMethod("info")
            ),
            AutoMappingRegistration(
                null,
                RequestMappingInfo
                    .paths("/submit")
                    .consumes(MediaType.APPLICATION_JSON_VALUE)
                    .methods(RequestMethod.POST)
                    .options(options)
                    .build(),
                beanType = User::class.java,
                bean,
                bean.javaClass.getMethod("submit", User::class.java)
            ),
        )
    }

    override fun isSupported(bean: Any, beanName: String): Boolean {
        return true
    }

}