plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

kotlin {
    jvmToolchain(17)
}
dependencies {
    api(project(":pdf-snapshot-core"))
    api(project(":pdf-snapshot-runtime"))
    compileOnly("org.junit.jupiter:junit-jupiter-api:5.10.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")

    implementation("org.junit.jupiter:junit-jupiter")
    implementation("org.junit.platform:junit-platform-launcher")
    // Para gerar PDFs de teste
    testImplementation("com.github.librepdf:openpdf:1.3.30")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "pdf-snapshot-junit"
        }
    }
}
