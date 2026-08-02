package nxcloud.ext.springmvc.automapping.annotation

import nxcloud.ext.springmvc.automapping.base.annotation.AutoMappingContract
import nxcloud.ext.springmvc.automapping.contract.AutoMappingContractData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.core.type.AnnotationMetadata

class AutoMappingBeanDefinitionRegistrarTest {

    private val registrar = AutoMappingBeanDefinitionRegistrar()

    @Test
    fun `methods expands one contract method into GET and POST data`() {
        val contractData = contractData("combined")

        assertEquals(
            listOf(AutoMappingContract.Method.GET, AutoMappingContract.Method.POST),
            contractData.map { it.method },
        )
        assertEquals(
            listOf("/contract/combined", "/contract/combined"),
            contractData.map { it.paths.single() },
        )
    }

    @Test
    fun `methods takes precedence over legacy method`() {
        val contractData = contractData("precedence")

        assertEquals(listOf(AutoMappingContract.Method.GET), contractData.map { it.method })
    }

    @Test
    fun `legacy method is used when methods is empty`() {
        val contractData = contractData("legacy")

        assertEquals(listOf(AutoMappingContract.Method.GET), contractData.map { it.method })
    }

    private fun contractData(methodName: String): List<AutoMappingContractData> {
        val metadata = AnnotationMetadata.introspect(TestContract::class.java)
        val typeAttributes = metadata.annotations
            .filter { it.type == AutoMappingContract::class.java }
            .first()
            .asAnnotationAttributes()
        val methodMetadata = metadata
            .getAnnotatedMethods(AutoMappingContract::class.java.canonicalName)
            .first { it.methodName == methodName }
        val methodAttributes = methodMetadata.annotations
            .filter { it.type == AutoMappingContract::class.java }
            .first()
            .asAnnotationAttributes()

        return registrar.annotationAttributesToContractData(
            metadata,
            typeAttributes.getStringArray("paths"),
            methodAttributes,
            methodName,
        )
    }

    @AutoMappingContract(paths = ["/contract"])
    private interface TestContract {

        @AutoMappingContract(
            method = AutoMappingContract.Method.POST,
            methods = [AutoMappingContract.Method.GET, AutoMappingContract.Method.POST],
            beanType = TestHandler::class,
        )
        fun combined()

        @AutoMappingContract(
            method = AutoMappingContract.Method.POST,
            methods = [AutoMappingContract.Method.GET],
            beanType = TestHandler::class,
        )
        fun precedence()

        @AutoMappingContract(
            method = AutoMappingContract.Method.GET,
            beanType = TestHandler::class,
        )
        fun legacy()
    }

    private class TestHandler {
        fun combined() = Unit

        fun precedence() = Unit

        fun legacy() = Unit
    }
}
