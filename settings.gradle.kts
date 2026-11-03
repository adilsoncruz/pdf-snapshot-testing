pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.0.21"
        id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
        id("io.gitlab.arturbosch.detekt") version "1.23.8"
    }
}

plugins {
    id("com.gradleup.nmcp.settings").version("1.6.2")
}

rootProject.name = "pdf-snapshot-testing"

include(
    ":pdf-snapshot-core",
    ":pdf-snapshot-junit",
    ":pdf-snapshot-kotest",
    ":pdf-snapshot-runtime",
)

nmcpSettings {
    centralPortal {
        username = System.getenv("SONATYPE_USERNAME") ?: ""
        password = System.getenv("SONATYPE_PASSWORD") ?: ""
        publishingType = "AUTOMATIC"
    }
}
