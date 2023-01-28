package nxcloud.ext.springmvc.automapping.contract

import mu.KotlinLogging
import nxcloud.ext.springmvc.automapping.base.annotation.AutoMappingContract
import nxcloud.ext.springmvc.automapping.spi.AutoMappingContractDataConverter
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRegistration
import nxcloud.ext.springmvc.automapping.spring.AutoMappingRequestParameterTypeBinding
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import org.springframework.web.util.pattern.PathPatternParser

/**
 * 注册接口声明的自定义映射
 */
open class AutoMappingContractRegistrar(
    private val contractData: List<AutoMappingContractData>,
) : InitializingBean {

    private val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired(required = false)
    private var converters: List<AutoMappingContractDataConverter>? = null

    @Autowired
    private lateinit var autoMappingRequestParameterTypeBinding: AutoMappingRequestParameterTypeBinding

    @Autowired
    private lateinit var requestMappingHandlerMapping: RequestMappingHandlerMapping

    override fun afterPropertiesSet() {
        val options = RequestMappingInfo.BuilderConfiguration()
        options.patternParser = PathPatternParser()

        contractData
            .map { data ->
                converters
                    ?.firstOrNull { converter ->
                        converter.isSupported(data)
                    }
                    ?.convert(data)
                    ?: data
            }
            .filter {
                it.beanMethod.isNotEmpty()
            }
            .map { data ->
                val bean = applicationContext.getBean(data.beanType)
                AutoMappingRegistration(
                    data.declaringMethod,
                    RequestMappingInfo
                        .paths(*data.paths)
                        .consumes(*data.consumes)
                        .methods(*convertMethods(data))
                        .options(options)
                        .build(),
                    bean,
                    bean.javaClass.methods
                        .first {
                            it.name == data.beanMethod
                        }
                )
            }
            .onEach { registration ->
                // 逐个注册
                requestMappingHandlerMapping.registerMapping(
                    registration.mapping,
                    registration.bean,
                    registration.method
                )
                autoMappingRequestParameterTypeBinding.registerBinding(
                    registration.method,
                    registration.declaringMethod,
                )
                logger.info {
                    "注册自动映射: ${registration.bean.javaClass.canonicalName} - ${registration.mapping}"
                }
            }
    }

    private fun convertMethods(data: AutoMappingContractData): Array<RequestMethod> {
        return when (data.method) {
            AutoMappingContract.Method.POST -> arrayOf(RequestMethod.POST)
            AutoMappingContract.Method.GET -> arrayOf(RequestMethod.GET)
        }
    }
}