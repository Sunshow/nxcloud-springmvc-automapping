package nxcloud.ext.springmvc.automapping.handler

import org.springframework.web.servlet.mvc.method.RequestMappingInfo

interface AutoMappingBeanHandler {

    /**
     * 解析要自动映射的 Bean 并生成对应的映射信息
     */
    fun mapping(bean: Any, beanName: String): List<RequestMappingInfo>

}