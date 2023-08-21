package nxcloud.ext.springmvc.automapping.contract

import nxcloud.ext.springmvc.automapping.base.annotation.AutoMappingContract
import java.lang.reflect.Method

data class AutoMappingContractData(
    // 声明处的方法
    val declaringMethod: Method,
    // 映射路径
    val paths: Array<String>,
    // 映射的HTTP请求方法
    val method: AutoMappingContract.Method,
    // 响应处理的Bean类型
    val beanType: Class<*>,
    // 响应处理的Bean方法名, 暂不支持多个同名方法
    val beanMethod: String = "",
    // 消费 Content-Type 类型
    val consumes: Array<String>,
    // 接口名称
    val summary: String = "",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AutoMappingContractData

        if (!paths.contentEquals(other.paths)) return false
        if (method != other.method) return false
        if (beanType != other.beanType) return false
        if (beanMethod != other.beanMethod) return false
        if (!consumes.contentEquals(other.consumes)) return false
        if (summary != other.summary) return false

        return true
    }

    override fun hashCode(): Int {
        var result = paths.contentHashCode()
        result = 31 * result + method.hashCode()
        result = 31 * result + beanType.hashCode()
        result = 31 * result + beanMethod.hashCode()
        result = 31 * result + consumes.contentHashCode()
        result = 31 * result + summary.hashCode()
        return result
    }
}