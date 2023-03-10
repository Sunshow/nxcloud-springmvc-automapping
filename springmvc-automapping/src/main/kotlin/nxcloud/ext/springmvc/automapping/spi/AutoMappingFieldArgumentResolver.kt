package nxcloud.ext.springmvc.automapping.spi

import org.springframework.core.MethodParameter
import org.springframework.web.context.request.NativeWebRequest
import java.lang.reflect.Field

/**
 * 解析入参对象中的自定义类型的属性字段
 */
interface AutoMappingFieldArgumentResolver {

    fun supportsParameter(
        field: Field, parameterObj: Any?,
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest,
    ): Boolean

    fun resolveArgument(
        field: Field, parameterObj: Any?,
        parameter: MethodParameter,
        resolvedParameterType: Class<*>,
        webRequest: NativeWebRequest,
    ): Any?

}