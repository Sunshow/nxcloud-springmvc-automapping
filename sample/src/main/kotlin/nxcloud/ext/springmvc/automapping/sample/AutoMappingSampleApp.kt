package nxcloud.ext.springmvc.automapping.sample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AutoMappingSampleApp

fun main(args: Array<String>) {
    runApplication<AutoMappingSampleApp>(*args)
}