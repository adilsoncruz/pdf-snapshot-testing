rootProject.name = "pdf-snapshot-testing"

include(
  ":pdf-snapshot-core",
  ":pdf-snapshot-runtime",
  ":pdf-snapshot-junit",
)

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
  }
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.0.21"
        id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
        id("io.gitlab.arturbosch.detekt") version "1.23.7"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
