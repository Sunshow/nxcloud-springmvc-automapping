package nxcloud.ext.springmvc.automapping.annotation

import mu.KotlinLogging
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.util.ClassUtils

class SpringMvcAutoMappingBeanPostProcessor(
    private val basePackages: Array<String>,
    autoMappingBeanType: Array<Class<*>>,
) : BeanPostProcessor {

    private val logger = KotlinLogging.logger {}

    private val autoMappingBeanType: Set<Class<*>> = autoMappingBeanType.toSet()

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (!isProcessor(bean, beanName)) {
            return bean
        }
        logger.info {
            "处理自动映射 Bean: $beanName - ${bean.javaClass.canonicalName}"
        }

        return bean
    }

    private fun isProcessor(bean: Any, beanName: String): Boolean {
        if (
            !basePackages
                .any {
                    ClassUtils.getPackageName(bean.javaClass).startsWith(it)
                }
        ) {
            return false
        }

        if (
            !autoMappingBeanType
                .any {
                    it.isAssignableFrom(bean.javaClass)
                }
        ) {
            return false
        }

        return true
    }
}