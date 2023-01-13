package nxcloud.ext.springmvc.automapping.sample

import nxcloud.ext.springmvc.automapping.annotation.EnableSpringMvcAutoMapping
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableSpringMvcAutoMapping
@SpringBootApplication
class AutoMappingSampleApp

fun main(args: Array<String>) {
    runApplication<AutoMappingSampleApp>(*args)
}