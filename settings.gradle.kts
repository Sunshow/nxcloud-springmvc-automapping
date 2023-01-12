rootProject.name = "nxcloud-springmvc-automapping"
include(":springmvc-automapping")
include(":ext-spring-boot-starter-springmvc-automapping")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")