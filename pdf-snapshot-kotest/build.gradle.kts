import org.gradle.api.tasks.SourceSetContainer

plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    compileOnly(project(":pdf-snapshot-core"))
    compileOnly(project(":pdf-snapshot-runtime"))

    compileOnly("io.kotest:kotest-framework-engine-jvm:5.9.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.9.1")
    testImplementation(project(":pdf-snapshot-core"))
    testImplementation(project(":pdf-snapshot-runtime"))

    testImplementation("com.github.librepdf:openpdf:1.3.30")
}

tasks.test {
    useJUnitPlatform()

    afterTest { descriptor, result ->
        val icon = when (result.resultType) {
            TestResult.ResultType.SUCCESS -> "✓"
            TestResult.ResultType.FAILURE -> "✗"
            TestResult.ResultType.SKIPPED -> "○"
        }

        println("$icon ${descriptor.displayName}")
    }
}

tasks.jar {
    dependsOn(":pdf-snapshot-core:classes", ":pdf-snapshot-runtime:classes")

    val coreOutput =
        project(":pdf-snapshot-core")
            .extensions
            .getByType<SourceSetContainer>()
            .named("main")
            .get()
            .output

    val runtimeOutput =
        project(":pdf-snapshot-runtime")
            .extensions
            .getByType<SourceSetContainer>()
            .named("main")
            .get()
            .output

    from(coreOutput)
    from(runtimeOutput)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "pdf-snapshot-kotest"
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            val repository =
                (findProperty("githubRepository") as String?)
                    ?: System.getenv("GITHUB_REPOSITORY")
                    ?: "adilsoncruz/pdf-snapshot-testing"

            url = uri("https://maven.pkg.github.com/$repository")

            credentials {
                username = (findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR")
                password = (findProperty("gpr.key") as String?) ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
