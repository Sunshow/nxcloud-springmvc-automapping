package nxcloud.ext.springmvc.automapping.sample

import io.github.oshai.kotlinlogging.KotlinLogging
import nxcloud.ext.springmvc.automapping.contract.AutoMappingContractData
import nxcloud.ext.springmvc.automapping.spi.AutoMappingContractDataConverter
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRegistration
import nxcloud.ext.springmvc.automapping.spi.AutoMappingRequestParameterTypeResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CommonAutoMappingConfig {

    private val logger = KotlinLogging.logger {}

    /**
     * 将针对 UseCase 的自动映射处理方法名统一解析到 execute() 方法上
     */
    @Bean
    protected fun abstractUseCaseAutoMappingContractDataConverter(): AutoMappingContractDataConverter {
        return object : AutoMappingContractDataConverter {
            override fun convert(data: AutoMappingContractData): AutoMappingContractData {
                return data.copy(
                    beanMethod = "execute"
                )
            }

            override fun isSupported(data: AutoMappingContractData): Boolean {
                return AbstractUseCase::class.java.isAssignableFrom(data.beanType)
            }
        }
    }

    /**
     * 将 UseCase 的抽象入参类型解析为具体的实现类的入参类型
     */
    @Bean
    protected fun abstractUseCaseAutoMappingRequestParameterTypeResolver(): AutoMappingRequestParameterTypeResolver {
        return object : AutoMappingRequestParameterTypeResolver {
            override fun isSupported(registration: AutoMappingRegistration): Boolean {
                return AbstractUseCase::class.java.isAssignableFrom(registration.beanType)
            }

            override fun resolveParameterType(registration: AutoMappingRegistration): Array<Class<*>> {
                // 固定只有一个内部类参数
                return arrayOf(Class.forName("${registration.beanType.canonicalName}\$Input"))
            }
        }
    }

}