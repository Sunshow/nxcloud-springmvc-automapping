package nxcloud.ext.springmvc.automapping.base.annotation

import kotlin.reflect.KClass

/**
 * 默认用于标识遵守自动映射协议的接口的注解
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AutoMappingContract(
    // 映射路径
    val paths: Array<String> = [],
    // 映射的HTTP请求方法
    val method: Method = Method.POST,
    // 响应处理的Bean类型
    val beanType: KClass<*> = Unit::class,
    // 响应处理的Bean方法名, 暂不支持多个同名方法
    val beanMethod: String = "",
) {
    enum class Method {
        GET,
        POST,
    }

}
