plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "backend-testing-demo"
include("spring-boot")
include("http4k-demo")
include("ktor-demo")
