package nxcloud.ext.springmvc.automapping.spi

/**
 * 解析自动映射的方法中的实际参数类型
 */
interface AutoMappingRequestParameterTypeResolver {

    /**
     * 用于将处理方法的入参解析为实际的请求参数类型, 例如将抽象类型参数处理成具体的类型
     */
    fun resolveParameterType(registration: AutoMappingRegistration): Array<Class<*>> {
        return emptyArray()
    }

    /**
     * 是否支持参数解析
     */
    fun isSupported(registration: AutoMappingRegistration): Boolean {
        return false
    }

}