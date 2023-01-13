package nxcloud.ext.springmvc.automapping.spring

import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.handler.AutoMappingBeanHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.util.ClassUtils
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import javax.annotation.PostConstruct


class SpringMvcAutoMappingBeanPostProcessor(
    private val basePackages: Array<String>,
    private val autoMappingBeanTypes: Array<Class<*>>,
) : BeanPostProcessor {

    private val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var context: ApplicationContext

    private val autoMappingBeanHandlers: MutableList<AutoMappingBeanHandler> = mutableListOf()

    @PostConstruct
    private fun init() {
        autoMappingBeanHandlers.addAll(
            context.getBeansOfType(AutoMappingBeanHandler::class.java).values
        )
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (!isSupported(bean, beanName)) {
            return bean
        }
        logger.info {
            "处理自动映射 Bean: $beanName - ${bean.javaClass.canonicalName}"
        }

        val handlerMapping = context.getBean(RequestMappingHandlerMapping::class.java)

        autoMappingBeanHandlers.forEach {
            it.mapping(bean, beanName).forEach { mapping ->
                // TODO DELETE ME
                handlerMapping.registerMapping(mapping, bean, bean.javaClass.methods[0])
            }
        }

        return bean
    }

    private fun isSupported(bean: Any, beanName: String): Boolean {
        if (
            !basePackages
                .any {
                    ClassUtils.getPackageName(bean.javaClass).startsWith(it)
                }
        ) {
            return false
        }

        if (
            !autoMappingBeanTypes
                .any {
                    it.isAssignableFrom(bean.javaClass)
                }
        ) {
            return false
        }

        return true
    }
}