dependencies {
    api(project(":springmvc-automapping"))
    api("org.springframework.boot:spring-boot-autoconfigure")

    compileOnly("org.springframework:spring-webmvc")
    compileOnly("com.fasterxml.jackson.core:jackson-databind")
}
