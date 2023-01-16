rootProject.name = "nxcloud-springmvc-automapping"
include(":springmvc-automapping-base")
include(":springmvc-automapping")
include(":ext-spring-boot-starter-springmvc-automapping")
include(":sample")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
