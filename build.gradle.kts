import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    signing
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.noarg)
    alias(libs.plugins.springboot) apply false
}

allprojects {
    group = "net.sunshow.nxcloud"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
        google()
    }

    tasks.create("downloadDependencies") {
        description = "Download all dependencies to the Gradle cache"
        doLast {
            for (configuration in configurations) {
                if (configuration.isCanBeResolved) {
                    configuration.files
                }
            }
        }
    }

    normalization {
        runtimeClasspath {
            metaInf {
                ignoreAttribute("Bnd-LastModified")
            }
        }
    }
}

/** Configure building for Java+Kotlin projects. */
subprojects {
    val project = this@subprojects

    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.allopen")
    apply(plugin = "org.jetbrains.kotlin.plugin.noarg")

    if (project.name == "sample") {
        apply(plugin = "org.springframework.boot")
    } else {
        apply(plugin = "java-library")
    }

    allOpen {
        annotations(
            "org.springframework.context.annotation.Configuration",
        )
    }

    noArg {
    }

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.toString()
    }

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
            freeCompilerArgs = listOf(
                "-Xjvm-default=all",
            )
        }
    }

    val testJavaVersion = System.getProperty("test.java.version", "17").toInt()

    tasks.withType<Test> {
        useJUnitPlatform()
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
        jvmArgs = jvmArgs!! + listOf(
            "-XX:+HeapDumpOnOutOfMemoryError"
        )

        val javaToolchains = project.extensions.getByType<JavaToolchainService>()
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(testJavaVersion))
        })

        maxParallelForks = Runtime.getRuntime().availableProcessors() * 2
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
        }

        systemProperty("junit.jupiter.extensions.autodetection.enabled", "true")
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }

    dependencies {
        implementation(platform(rootProject.libs.bom.springboot))
        implementation("org.slf4j:slf4j-api")
        implementation(rootProject.libs.kotlin.logging.jvm)
        testImplementation("org.jetbrains.kotlin:kotlin-test")
        testImplementation("org.junit.jupiter:junit-jupiter-api")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
        testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    }

}

subprojects {
    if (project.name == "sample") {
        return@subprojects
    }

    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    publishing {

        // 发布 release
        version = "1.0.0"

        val sourcesJar by tasks.registering(Jar::class) {
            archiveClassifier.set("sources")
            from(sourceSets.main.get().allSource)
        }

        val javadocJar by tasks.registering(Jar::class) {
            archiveClassifier.set("javadoc")
            from(tasks.javadoc)
        }

        publications {
            create<MavenPublication>("mavenJava") {
                artifactId = "nxcloud-${project.name}"

                from(components["java"])

                artifact(sourcesJar.get())

                artifact(javadocJar.get())

                pom {
                    name.set("NXCloud SpringMVC AutoMapping")
                    description.set("A convenient library for auto registering SpringMVC RequestMappings")
                    url.set("https://github.com/Sunshow/nxcloud-springmvc-automapping")
                    properties.set(
                        mapOf(
                        )
                    )
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("sunshow")
                            name.set("Sunshow")
                            email.set("sunshow@gmail.com")
                        }
                    }
                    scm {
                        connection.set("https://github.com/Sunshow/nxcloud-springmvc-automapping")
                        developerConnection.set("https://github.com/Sunshow/nxcloud-springmvc-automapping")
                        url.set("https://github.com/Sunshow/nxcloud-springmvc-automapping")
                    }
                }
            }
        }

        if (project.hasProperty("publishUsername") && project.hasProperty("publishPassword")
            && project.hasProperty("publishReleasesRepoUrl") && project.hasProperty("publishSnapshotsRepoUrl")
        ) {
            repositories {
                maven {
                    val publishReleasesRepoUrl: String by project
                    val publishSnapshotsRepoUrl: String by project

                    url = uri(
                        if (version.toString().endsWith("SNAPSHOT")) publishSnapshotsRepoUrl else publishReleasesRepoUrl
                    )
                    isAllowInsecureProtocol = true

                    val publishUsername: String by project
                    val publishPassword: String by project
                    credentials {
                        username = publishUsername
                        password = publishPassword
                    }
                }
            }
        }
    }

    signing {
        sign(publishing.publications["mavenJava"])
    }

}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}