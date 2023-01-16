package nxcloud.ext.springmvc.automapping.spring.boot.annotation

import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.context.AutoMappingContext
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata
import org.springframework.util.ClassUtils

class AutoMappingBeanDefinitionRegistrar : ImportBeanDefinitionRegistrar {

    private val logger = KotlinLogging.logger {}

    private val contextBeanName = "nxAutoMappingContext"

    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        super.registerBeanDefinitions(importingClassMetadata, registry)

        logger.info {
            "启用 SpringMvc 自动映射, 注册处理器"
        }

        val attributes =
            importingClassMetadata.getAnnotationAttributes(NXEnableSpringMvcAutoMapping::class.java.canonicalName)!!

        // 获取扫描包路径
        val basePackages = getBasePackages(importingClassMetadata, attributes)
        logger.info {
            "自动映射扫描包路径: ${basePackages.joinToString(", ")}"
        }

        // 注册 Context
        registry.registerBeanDefinition(
            contextBeanName,
            BeanDefinitionBuilder
                .genericBeanDefinition(
                    AutoMappingContext::class.java
                )
                .addConstructorArgValue(basePackages)
                .addConstructorArgValue(attributes["autoMappingAnnotations"])
                .addConstructorArgValue(attributes["autoMappingBeanTypes"])
                .beanDefinition
        )

        logger.info {
            "启用 SpringMvc 自动映射, 处理器注册完成"
        }
    }

    private fun getBasePackages(
        importingClassMetadata: AnnotationMetadata,
        attributes: Map<String, Any>
    ): Array<String> {
        @Suppress("UNCHECKED_CAST")
        val basePackages = attributes["basePackages"]!! as Array<String>
        return if (basePackages.isEmpty()) {
            val packageName = ClassUtils.getPackageName(importingClassMetadata.className)
            arrayOf(packageName)
        } else {
            basePackages
        }
    }

}