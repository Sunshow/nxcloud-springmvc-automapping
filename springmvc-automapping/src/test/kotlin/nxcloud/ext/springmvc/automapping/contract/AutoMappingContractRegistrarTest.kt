package nxcloud.ext.springmvc.automapping.contract

import nxcloud.ext.springmvc.automapping.base.annotation.AutoMappingContract
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.util.pattern.PathPatternParser

class AutoMappingContractRegistrarTest {

    private val registrar = AutoMappingContractRegistrar(emptyList())

    private val options = RequestMappingInfo.BuilderConfiguration().apply {
        patternParser = PathPatternParser()
    }

    @Test
    fun `legacy method remains supported`() {
        val mapping = registrar.createMapping(
            contractData(method = AutoMappingContract.Method.GET),
            options,
        )

        assertEquals(setOf(RequestMethod.GET), mapping.methodsCondition.methods)
    }

    @Test
    fun `default consumes accepts bodyless GET and JSON POST`() {
        val getMapping = registrar.createMapping(
            contractData(method = AutoMappingContract.Method.GET),
            options,
        )
        val postMapping = registrar.createMapping(
            contractData(method = AutoMappingContract.Method.POST),
            options,
        )

        assertNotNull(
            getMapping.consumesCondition.getMatchingCondition(
                MockHttpServletRequest("GET", "/endpoint")
            )
        )
        assertNotNull(
            postMapping.consumesCondition.getMatchingCondition(
                requestWithBody("POST", MediaType.APPLICATION_JSON_VALUE)
            )
        )
        assertNull(
            postMapping.consumesCondition.getMatchingCondition(
                requestWithBody("POST", MediaType.TEXT_PLAIN_VALUE)
            )
        )
    }

    @Test
    fun `empty consumes continues to accept JSON POST`() {
        val mapping = registrar.createMapping(
            contractData(
                method = AutoMappingContract.Method.POST,
                consumes = emptyArray(),
            ),
            options,
        )

        assertNotNull(
            mapping.consumesCondition.getMatchingCondition(
                requestWithBody("POST", MediaType.APPLICATION_JSON_VALUE)
            )
        )
    }

    private fun contractData(
        method: AutoMappingContract.Method = AutoMappingContract.Method.POST,
        consumes: Array<String> = arrayOf(MediaType.APPLICATION_JSON_VALUE),
    ): AutoMappingContractData {
        return AutoMappingContractData(
            declaringMethod = TestContract::class.java.getDeclaredMethod("endpoint"),
            paths = arrayOf("/endpoint"),
            method = method,
            beanType = TestHandler::class.java,
            beanMethod = "endpoint",
            consumes = consumes,
        )
    }

    private fun requestWithBody(method: String, contentType: String): MockHttpServletRequest {
        val body = "{}".toByteArray()
        return MockHttpServletRequest(method, "/endpoint").apply {
            setContent(body)
            this.contentType = contentType
            addHeader(HttpHeaders.CONTENT_LENGTH, body.size)
        }
    }

    private interface TestContract {
        fun endpoint()
    }

    private class TestHandler {
        fun endpoint() = Unit
    }
}
