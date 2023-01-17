package nxcloud.ext.springmvc.automapping.contract

import nxcloud.ext.springmvc.automapping.base.annotation.AutoMappingContract

class AutoMappingContractData(
    // 映射路径
    val paths: Array<String>,
    // 映射的HTTP请求方法
    val method: AutoMappingContract.Method,
    // 响应处理的Bean类型
    val beanType: Class<*>,
    // 响应处理的Bean方法名, 暂不支持多个同名方法
    val beanMethod: String = "",
)