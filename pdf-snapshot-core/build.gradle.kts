plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("org.apache.pdfbox:pdfbox:3.0.8")
    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")

    testImplementation("com.github.librepdf:openpdf:3.0.5")
}
