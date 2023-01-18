package nxcloud.ext.springmvc.automapping.spi

/**
 * 解析加了 @AutoMappingBean 或其他指定的自定义 注解的 Bean 包含的需要自动映射的信息
 * 由使用者自行实现
 */
interface AutoMappingBeanRequestResolver {

    /**
     * 解析要自动映射的 Bean 并生成对应的映射信息
     */
    fun resolveMapping(bean: Any, beanName: String): List<AutoMappingRegistration>

    /**
     * 是否支持映射
     */
    fun isSupported(bean: Any, beanName: String): Boolean {
        return false
    }

}