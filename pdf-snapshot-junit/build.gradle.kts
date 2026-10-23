import org.gradle.api.tasks.SourceSetContainer

plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

kotlin {
    jvmToolchain(21)
}

java {
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    compileOnly(project(":pdf-snapshot-core"))
    compileOnly(project(":pdf-snapshot-runtime"))

    compileOnly("org.junit.jupiter:junit-jupiter-api:5.11.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    testImplementation("org.junit.platform:junit-platform-launcher:1.11.4")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation(project(":pdf-snapshot-core"))
    testImplementation(project(":pdf-snapshot-runtime"))

    // Para gerar PDFs de teste
    testImplementation("com.github.librepdf:openpdf:1.3.43")
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
