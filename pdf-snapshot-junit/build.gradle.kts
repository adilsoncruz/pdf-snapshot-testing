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

    compileOnly("org.junit.jupiter:junit-jupiter-api:5.10.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation(project(":pdf-snapshot-core"))
    testImplementation(project(":pdf-snapshot-runtime"))

    // Para gerar PDFs de teste
    testImplementation("com.github.librepdf:openpdf:1.3.30")
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
            artifactId = "pdf-snapshot-junit"
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
