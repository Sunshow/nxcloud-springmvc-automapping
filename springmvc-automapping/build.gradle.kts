dependencies {
    api(project(":springmvc-automapping-base"))

    compileOnly(libs.jakarta.annotation.api)
    compileOnly(libs.jakarta.servlet.api)

    compileOnly(libs.spring.context)
    compileOnly(libs.spring.mvc)
    compileOnly(libs.jackson.databind)
}
