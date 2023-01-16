package nxcloud.ext.springmvc.automapping.sample

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @RequestMapping("/test")
    fun test(): String {
        return "Hello World"
    }

    @RequestMapping("/void")
    fun testVoid() {
        println("testVoid")
    }


    @RequestMapping("/test1")
    fun test1(param: Param): String {
        return "$param"
    }

    @RequestMapping("/test2")
    fun test2(a: String, b: Int): String {
        return a
    }

    data class Param(
        val a: String,
        val b: Int,
    )
}