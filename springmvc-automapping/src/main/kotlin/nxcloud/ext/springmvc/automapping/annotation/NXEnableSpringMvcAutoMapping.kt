package nxcloud.ext.springmvc.automapping.annotation

import nxcloud.ext.springmvc.automapping.spring.AutoMappingBeanDefinitionRegistrar
import org.springframework.context.annotation.Import
import java.lang.annotation.*
import kotlin.reflect.KClass

/**
 * 启用自动映射的注解
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(AutoMappingBeanDefinitionRegistrar::class)
annotation class NXEnableSpringMvcAutoMapping(
    // 自动映射扫描的包路径
    val basePackages: Array<String> = [],
    val autoMappingAnnotations: Array<KClass<*>> = [AutoMappingContract::class],
    // 要做自动映射处理的 Bean 类型
    val autoMappingBeanTypes: Array<KClass<*>> = [],
)
