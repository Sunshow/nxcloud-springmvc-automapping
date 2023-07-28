package nxcloud.ext.springmvc.automapping.spring

import io.github.oshai.kotlinlogging.KotlinLogging
import nxcloud.ext.springmvc.automapping.context.AutoMappingContext
import nxcloud.ext.springmvc.automapping.spi.AutoMappingBeanRequestResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Lazy
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping


class AutoMappingRequestHandlerRegistrar : BeanPostProcessor {

    private val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var autoMappingContext: AutoMappingContext

    @Autowired(required = false)
    private var autoMappingBeanRequestResolvers: List<AutoMappingBeanRequestResolver>? = null

    @Autowired(required = false)
    private lateinit var autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding

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

        autoMappingBeanRequestResolvers
            ?.forEach {
                if (it.isSupported(bean, beanName)) {
                    it.resolveMapping(bean, beanName)
                        .forEach { registration ->
                            // 逐个注册
                            requestMappingHandlerMapping.registerMapping(
                                registration.mapping,
                                registration.bean,
                                registration.method
                            )
                            autoMappingRequestParameterTypeBinding.registerBinding(
                                registration.method,
                                registration.declaringMethod,
                                registration.mapping
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