package nxcloud.ext.springmvc.automapping.sample

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @RequestMapping("/test")
    fun test(): String {
        return "Hello World"
    }

}