package nxcloud.ext.springmvc.automapping.annotation

import io.github.oshai.kotlinlogging.KotlinLogging
import nxcloud.ext.springmvc.automapping.base.annotation.AutoMappingContract
import nxcloud.ext.springmvc.automapping.context.AutoMappingContext
import nxcloud.ext.springmvc.automapping.contract.AutoMappingContractData
import nxcloud.ext.springmvc.automapping.contract.AutoMappingContractRegistrar
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.context.annotation.ScannedGenericBeanDefinition
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.env.Environment
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.util.ClassUtils

class AutoMappingBeanDefinitionRegistrar : ImportBeanDefinitionRegistrar, EnvironmentAware {

    private val logger = KotlinLogging.logger {}

    private val contextBeanName = "nxAutoMappingContext"

    private val contractRegistrarBeanName = "nxAutoMappingContractRegistrar"

    private lateinit var environment: Environment

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

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

        // using these packages, scan for interface annotated with MyCustomBean
        val provider = object : ClassPathScanningCandidateComponentProvider(false, environment) {
            // Override isCandidateComponent to only scan for interface
            override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
                val metadata: AnnotationMetadata = beanDefinition.metadata
                return metadata.isIndependent && metadata.isInterface
            }
        }
        provider.addIncludeFilter(AnnotationTypeFilter(AutoMappingContract::class.java))

        val contractData = mutableListOf<AutoMappingContractData>()

        // Scan all packages
        for (basePackage in basePackages) {
            for (beanDefinition in provider.findCandidateComponents(basePackage)) {
                val metadata = (beanDefinition as ScannedGenericBeanDefinition).metadata
                val typeAttributes = metadata.annotations
                    .filter {
                        it.type == AutoMappingContract::class.java
                    }
                    .map {
                        it.asAnnotationAttributes()
                    }
                // 拿到接口映射前缀
                val typePaths = typeAttributes.first().getStringArray("paths")

                // 拿到具体方法的映射信息
                val methodMetadata = metadata.getAnnotatedMethods(AutoMappingContract::class.java.canonicalName)
                methodMetadata
                    .forEach { mm ->
                        val methodAttributes = mm.annotations
                            .filter {
                                it.type == AutoMappingContract::class.java
                            }
                            .map {
                                it.asAnnotationAttributes()
                            }
                        contractData.add(
                            annotationAttributesToContractData(
                                metadata,
                                typePaths,
                                methodAttributes.first(),
                                mm.methodName
                            )
                        )
                    }
            }
        }

        // 注册供后续处理
        registry.registerBeanDefinition(
            contractRegistrarBeanName,
            BeanDefinitionBuilder
                .genericBeanDefinition(
                    AutoMappingContractRegistrar::class.java
                ) {
                    AutoMappingContractRegistrar(contractData.toList())
                }
                .beanDefinition
        )

        logger.info {
            "启用 SpringMvc 自动映射, 处理器注册完成"
        }
    }

    private fun annotationAttributesToContractData(
        typeMetadata: AnnotationMetadata,
        typePaths: Array<String>,
        methodAttribute: AnnotationAttributes,
        methodName: String,
    ): AutoMappingContractData {
        val method = methodAttribute.getEnum<AutoMappingContract.Method>("method")
        val beanType = methodAttribute.getClass<Any>("beanType")

        // 默认使用同名方法查找
        val beanMethod = methodAttribute.getString("beanMethod")
            .takeIf {
                it.isNotEmpty()
            }
            ?: methodName

        val paths = methodAttribute.getStringArray("paths").toMutableList()
        if (paths.isEmpty()) {
            // 如果方法未声明路径 默认使用方法名
            paths.add("/$methodName")
        }

        val consumes = methodAttribute.getStringArray("consumes")

        // 合并映射路径
        return typePaths
            .flatMap { prefix ->
                paths
                    .map { path ->
                        "$prefix$path"
                    }
            }
            .let {
                AutoMappingContractData(
                    Class.forName(typeMetadata.className).methods.first { m ->
                        // 找到声明处的方法
                        m.name == methodName
                    },
                    it.toTypedArray(),
                    method,
                    beanType,
                    beanMethod,
                    consumes,
                )
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