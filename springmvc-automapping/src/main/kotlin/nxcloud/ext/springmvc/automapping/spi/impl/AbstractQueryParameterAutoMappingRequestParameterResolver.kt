package nxcloud.ext.springmvc.automapping.spi.impl

import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestParameterResolver
import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import javax.servlet.http.HttpServletRequest

abstract class AbstractQueryParameterAutoMappingRequestParameterResolver : AutoMappingRequestParameterResolver {

    private val logger = KotlinLogging.logger {}

    override fun resolveParameter(
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest
    ): Any? {
        val servletRequest = webRequest.getNativeRequest(HttpServletRequest::class.java)!!

        // 默认调用空构造函数创建实例
        val parameterObj = resolvedParameterType.getDeclaredConstructor().newInstance()

        // 填充属性
        servletRequest.parameterMap.forEach { (key, value) ->
            try {
                val field = resolvedParameterType.getDeclaredField(key)
                field.isAccessible = true
                // 检查是否是数组
                // TODO 暂未处理集合类的情况
                if (field.type.isArray) {
                    field.set(parameterObj, value)
                } else {
                    field.set(parameterObj, value[0])
                }
            } catch (e: NoSuchFieldException) {
                // 忽略传递未知属性的情况
                logger.debug { "No such field: $key" }
            }
        }

        return parameterObj
    }

}