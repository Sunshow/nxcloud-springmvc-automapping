package nxcloud.ext.springmvc.automapping.context

import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.util.ClassUtils

class AutoMappingContext(
    // 自动映射扫描的包路径
    private val basePackages: Array<String>,
    // 要做自动映射处理的注解类型
    private val autoMappingAnnotations: Array<Class<out Annotation>>,
    // 要做自动映射处理的 Bean 类型, 如果配置了该属性, 则会忽略 [autoMappingAnnotations] 属性
    private val autoMappingBeanTypes: Array<Class<*>>,
) {
    fun isSupported(type: Class<*>): Boolean {
        if (
            !basePackages
                .any {
                    ClassUtils.getPackageName(type).startsWith(it)
                }
        ) {
            return false
        }

        if (autoMappingBeanTypes.isNotEmpty()) {
            if (
                !autoMappingBeanTypes
                    .any {
                        it.isAssignableFrom(type)
                    }
            ) {
                return false
            }
        } else if (autoMappingAnnotations.isNotEmpty()) {
            if (
                !autoMappingAnnotations
                    .any {
                        isAutoMappingAnnotationPresent(type, it)
                    }
            ) {
                return false
            }
        } else {
            return false
        }

        return true
    }

    private fun isAutoMappingAnnotationPresent(type: Class<*>, annotationClass: Class<out Annotation>): Boolean {
        val hasAnnotation = AnnotatedElementUtils.isAnnotated(type, annotationClass)
        if (hasAnnotation) {
            return true
        }
        // 查找接口上的注解
        return ClassUtils.getAllInterfacesForClass(type)
            .any {
                AnnotatedElementUtils.isAnnotated(it, annotationClass)
            }
    }
}