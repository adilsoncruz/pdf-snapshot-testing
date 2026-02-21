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
    compileOnly("org.junit.jupiter:junit-jupiter-api:5.10.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    // Para gerar PDFs de teste
    testImplementation("com.github.librepdf:openpdf:1.3.30")
}

