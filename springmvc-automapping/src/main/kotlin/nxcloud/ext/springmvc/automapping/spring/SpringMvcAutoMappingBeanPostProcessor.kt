package nxcloud.ext.springmvc.automapping.spring

import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.context.AutoMappingContext
import nxcloud.ext.springmvc.automapping.handler.AutoMappingBeanHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import javax.annotation.PostConstruct


class SpringMvcAutoMappingBeanPostProcessor : BeanPostProcessor {

    private val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var autoMappingContext: AutoMappingContext

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    private val autoMappingBeanHandlers: MutableList<AutoMappingBeanHandler> = mutableListOf()

    @PostConstruct
    private fun init() {
        autoMappingBeanHandlers.addAll(
            applicationContext.getBeansOfType(AutoMappingBeanHandler::class.java).values
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

        autoMappingBeanHandlers.forEach {
            it.mapping(bean, beanName).forEach { mapping ->
                // TODO DELETE ME
                handlerMapping.registerMapping(mapping, bean, bean.javaClass.methods[0])
            }
        }

        return bean
    }
    
}