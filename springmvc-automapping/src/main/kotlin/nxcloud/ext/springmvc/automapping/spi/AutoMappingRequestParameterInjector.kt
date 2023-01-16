package nxcloud.ext.springmvc.automapping.spi

interface AutoMappingRequestParameterInjector {

    /**
     * 注入属性
     */
    fun inject(parameters: Array<Any>) {
    }

}
