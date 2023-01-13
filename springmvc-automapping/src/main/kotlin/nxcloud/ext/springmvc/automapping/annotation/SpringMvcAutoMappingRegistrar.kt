package nxcloud.ext.springmvc.automapping.annotation

import mu.KotlinLogging
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata
import org.springframework.util.ClassUtils

class SpringMvcAutoMappingRegistrar : ImportBeanDefinitionRegistrar {

    private val logger = KotlinLogging.logger {}

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

        val postProcessorBuilder = BeanDefinitionBuilder.genericBeanDefinition(
            SpringMvcAutoMappingBeanPostProcessor::class.java
        )
        postProcessorBuilder.addConstructorArgValue(basePackages)
        postProcessorBuilder.addConstructorArgValue(attributes["autoMappingBeanTypes"])
        registry.registerBeanDefinition("nxSpringMvcAutoMappingBeanPostProcessor", postProcessorBuilder.beanDefinition)


//        //生成BeanDefinition并注册到容器中
//        val mappingBuilder: BeanDefinitionBuilder = BeanDefinitionBuilder
//            .genericBeanDefinition(ContractAutoHandlerRegisterHandlerMapping::class.java)
//        mappingBuilder.addConstructorArgValue(basePackages)
//        try {
//            val forName = Class.forName("com.dizang.concise.mvc.config.ConciseMvcRegisterConfig")
//            if (forName != null) {
//                mappingBuilder.addDependsOn("conciseMvcRegisterConfig")
//            }
//        } catch (e: ClassNotFoundException) {
//            log.error(e.message, e)
//        }
//        registry.registerBeanDefinition("contractAutoHandlerRegisterHandlerMapping", mappingBuilder.beanDefinition)
//
//        val processBuilder: BeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(
//            ContractReturnValueWebMvcConfigurer::class.java
//        )
//        registry.registerBeanDefinition("contractReturnValueWebMvcConfigurer", processBuilder.beanDefinition)

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