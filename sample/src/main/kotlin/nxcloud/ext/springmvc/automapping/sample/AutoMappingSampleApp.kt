package nxcloud.ext.springmvc.automapping.sample

import nxcloud.ext.springmvc.automapping.annotation.NXEnableSpringMvcAutoMapping
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@NXEnableSpringMvcAutoMapping
@SpringBootApplication
class AutoMappingSampleApp

fun main(args: Array<String>) {
    runApplication<AutoMappingSampleApp>(*args)
}