dependencies {
    api(project(":springmvc-automapping-base"))

    compileOnly("jakarta.annotation:jakarta.annotation-api")
    compileOnly("jakarta.servlet:jakarta.servlet-api")

    compileOnly("org.springframework:spring-context")
    compileOnly("org.springframework:spring-webmvc")
    compileOnly("com.fasterxml.jackson.core:jackson-databind")
}
