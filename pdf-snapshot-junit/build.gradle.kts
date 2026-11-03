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

    api("org.apache.pdfbox:pdfbox:3.0.8")
    compileOnly("org.junit.jupiter:junit-jupiter-api:5.11.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.22.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    testImplementation("org.junit.platform:junit-platform-launcher:1.11.4")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.4.0")
    testImplementation(project(":pdf-snapshot-core"))
    testImplementation(project(":pdf-snapshot-runtime"))

    // Para gerar PDFs de teste
    testImplementation("com.github.librepdf:openpdf:3.0.5")
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

tasks.named<Jar>("sourcesJar") {
    val coreSources =
        project(":pdf-snapshot-core")
            .extensions
            .getByType<SourceSetContainer>()
            .named("main")
            .get()
            .allSource

    val runtimeSources =
        project(":pdf-snapshot-runtime")
            .extensions
            .getByType<SourceSetContainer>()
            .named("main")
            .get()
            .allSource

    from(coreSources)
    from(runtimeSources)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "pdf-snapshot-junit"

            pom {
                name.set("PDF Snapshot Testing - JUnit 5")
                description.set("Visual snapshot testing library for PDFs on the JVM (JUnit 5 adapter)")
                url.set("https://github.com/adilsoncruz/pdf-snapshot-testing")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("adilsoncruz")
                        name.set("Adilson Cruz")
                        url.set("https://github.com/adilsoncruz")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/adilsoncruz/pdf-snapshot-testing.git")
                    developerConnection.set("scm:git:ssh://github.com:adilsoncruz/pdf-snapshot-testing.git")
                    url.set("https://github.com/adilsoncruz/pdf-snapshot-testing")
                }
            }
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
