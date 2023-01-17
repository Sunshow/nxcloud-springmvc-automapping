package nxcloud.ext.springmvc.automapping.spi

import nxcloud.ext.springmvc.automapping.contract.AutoMappingContractData

/**
 * 对于根据协议转换的数据在注册前进行必要的转换
 */
interface AutoMappingContractDataConverter {

    fun convert(data: AutoMappingContractData): AutoMappingContractData

    fun isSupported(data: AutoMappingContractData): Boolean {
        return true
    }
    
}