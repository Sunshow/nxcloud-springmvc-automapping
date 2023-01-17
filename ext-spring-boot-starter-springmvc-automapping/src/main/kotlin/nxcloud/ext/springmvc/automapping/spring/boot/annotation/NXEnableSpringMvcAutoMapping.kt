package nxcloud.ext.springmvc.automapping.spring.boot.annotation

import nxcloud.ext.springmvc.automapping.base.annotation.AutoMappingBean
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
    // 要做自动映射处理的注解类型
    val autoMappingAnnotations: Array<KClass<out Annotation>> = [AutoMappingBean::class],
    // 要做自动映射处理的 Bean 类型, 如果配置了该属性, 则会忽略 [autoMappingAnnotations] 属性
    val autoMappingBeanTypes: Array<KClass<*>> = [],
)
