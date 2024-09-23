package nxcloud.ext.springmvc.automapping.sample

import io.github.oshai.kotlinlogging.KotlinLogging
import nxcloud.foundation.core.lang.annotation.NoArgs
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Validated // 单纯为了让 Spring 增强一下好让 execute 执行时可以拿到实际实现类的 InputData 类型
abstract class AbstractUseCase<Input : AbstractUseCase.InputData, Output : AbstractUseCase.OutputData> {

    protected val logger = KotlinLogging.logger {}

    fun execute(input: Input): Output {
        return doAction(input)
    }

    protected abstract fun doAction(input: Input): Output

    abstract class InputData

    abstract class OutputData

}

@Component
class SampleUseCase : AbstractUseCase<SampleUseCase.Input, SampleUseCase.Output>() {
    override fun doAction(input: Input): Output {
        logger.info { "aLong: ${input.aLong}, bString: ${input.bString}" }

        return Output(
            a = input.aLong ?: 0,
            b = input.bString ?: "",
        )
    }

    @NoArgs
    class Input(
        val aLong: Long?,

        val bString: String?,
    ) : InputData()

    class Output(
        val a: Long,
        val b: String,
    ) : OutputData()
}
