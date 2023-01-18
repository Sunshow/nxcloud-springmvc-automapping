package nxcloud.ext.springmvc.automapping.spi.impl

import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestParameterResolver
import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import javax.servlet.http.HttpServletRequest

class FormUrlencodedAutoMappingRequestParameterResolver : AutoMappingRequestParameterResolver {

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
            val field = resolvedParameterType.getDeclaredField(key)
            field.isAccessible = true
            // 检查是否是数组
            // TODO 暂未处理集合类的情况
            if (field.type.isArray) {
                field.set(parameterObj, value)
            } else {
                field.set(parameterObj, value[0])
            }
        }

        return parameterObj
    }

    override fun isSupported(
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest
    ): Boolean {
        val servletRequest = webRequest.getNativeRequest(HttpServletRequest::class.java)!!

        return servletRequest.contentType?.startsWith("application/x-www-form-urlencoded") ?: false
    }

}