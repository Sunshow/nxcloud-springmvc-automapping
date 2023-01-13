package nxcloud.ext.springmvc.automapping.annotation

import kotlin.reflect.KClass

/**
 * 启用自动映射的注解
 */
annotation class EnableSpringMvcAutoMapping(
    val basePackages: Array<String> = [],
    val autoMappingAnnotation: Array<KClass<*>> = [AutoMappingContract::class],
)
