dependencies {
    api(project(":springmvc-automapping"))
    api(libs.springboot.autoconfigure)
    
    compileOnly(libs.spring.mvc)
}
