package nxcloud.ext.springmvc.automapping.context

import org.springframework.util.ClassUtils

class AutoMappingContext(
    // 自动映射扫描的包路径
    private val basePackages: Array<String>,
    private val autoMappingAnnotations: Array<Class<*>>,
    // 要做自动映射处理的 Bean 类型
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

        if (
            !autoMappingBeanTypes
                .any {
                    it.isAssignableFrom(type)
                }
        ) {
            return false
        }

        return true
    }
}