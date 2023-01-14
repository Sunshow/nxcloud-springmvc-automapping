package nxcloud.ext.springmvc.automapping.spring

import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.context.AutoMappingContext
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import javax.annotation.PostConstruct


class AutoMappingRequestHandlerRegistrar : BeanPostProcessor {

    private val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var autoMappingContext: AutoMappingContext

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    private val autoMappingRequestHandlers: MutableList<AutoMappingRequestHandler> = mutableListOf()

    @PostConstruct
    private fun init() {
        autoMappingRequestHandlers.addAll(
            applicationContext.getBeansOfType(AutoMappingRequestHandler::class.java).values
        )
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (!autoMappingContext.isSupported(bean.javaClass)) {
            return bean
        }
        logger.info {
            "处理自动映射 Bean: $beanName - ${bean.javaClass.canonicalName}"
        }

        val handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping::class.java)

        autoMappingRequestHandlers.forEach {
            it.mapping(bean, beanName).forEach { info ->
                // 逐个注册
                handlerMapping.registerMapping(info.mapping, info.bean, info.method)
                logger.info {
                    "注册自动映射: ${bean.javaClass.canonicalName} - ${info.mapping}"
                }
            }
        }

        return bean
    }

}