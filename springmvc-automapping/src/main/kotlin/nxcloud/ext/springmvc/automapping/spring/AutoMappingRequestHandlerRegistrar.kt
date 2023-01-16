package nxcloud.ext.springmvc.automapping.spring

import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.context.AutoMappingContext
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Lazy
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping


class AutoMappingRequestHandlerRegistrar(
    private val autoMappingRequestResolvers: List<AutoMappingRequestResolver>,
) : BeanPostProcessor {

    private val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var autoMappingContext: AutoMappingContext

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

        autoMappingRequestResolvers.forEach {
            if (it.isSupportedMapping(bean, beanName)) {
                it.resolveMapping(bean, beanName).forEach { resolved ->
                    // 逐个注册
                    requestMappingHandlerMapping.registerMapping(resolved.mapping, resolved.bean, resolved.method)
                    logger.info {
                        "注册自动映射: ${bean.javaClass.canonicalName} - ${resolved.mapping}"
                    }
                }
            }
        }

        return bean
    }

}