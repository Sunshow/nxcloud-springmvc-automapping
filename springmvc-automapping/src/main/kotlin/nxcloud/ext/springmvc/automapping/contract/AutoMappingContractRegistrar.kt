package nxcloud.ext.springmvc.automapping.contract

import nxcloud.ext.springmvc.automapping.spi.AutoMappingRegistration

class AutoMappingContractRegistrar(
    private val contractData: List<AutoMappingContractData>,
) {

    fun register(): List<AutoMappingRegistration> {
        return emptyList()
    }

}