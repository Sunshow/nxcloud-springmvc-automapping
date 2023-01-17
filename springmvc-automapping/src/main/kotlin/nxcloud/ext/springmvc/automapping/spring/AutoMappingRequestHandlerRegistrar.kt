package nxcloud.ext.springmvc.automapping.spring

import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.context.AutoMappingContext
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Lazy
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping


class AutoMappingRequestHandlerRegistrar : BeanPostProcessor {

    private val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var autoMappingContext: AutoMappingContext

    @Autowired(required = false)
    private var autoMappingRequestResolvers: List<AutoMappingRequestResolver>? = null

    @Lazy
    @Autowired
    private lateinit var requestMappingHandlerMapping: RequestMappingHandlerMapping

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (!autoMappingContext.isSupported(bean.javaClass)) {
            return bean
        }
        logger.info {
            "处理自动映射 Bean: $beanName - ${bean.javaClass.canonicalName}"
        }

        autoMappingRequestResolvers
            ?.forEach {
                if (it.isSupportedMapping(bean, beanName)) {
                    it.resolveMapping(bean, beanName)
                        .forEach { registration ->
                            // 逐个注册
                            requestMappingHandlerMapping.registerMapping(
                                registration.mapping,
                                registration.bean,
                                registration.method
                            )
                            logger.info {
                                "注册自动映射: ${bean.javaClass.canonicalName} - ${registration.mapping}"
                            }

                        }
                }
            }

        return bean
    }

}