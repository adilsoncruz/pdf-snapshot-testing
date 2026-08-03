plugins {
    kotlin("jvm")
    `java-library`
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api("org.apache.pdfbox:pdfbox:2.0.30")
    // Tests
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.0")

    testImplementation("com.github.librepdf:openpdf:1.3.30")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events(
            TestLogEvent.PASSED,
            TestLogEvent.FAILED,
            TestLogEvent.SKIPPED
        )

        exceptionFormat = TestExceptionFormat.FULL

        showExceptions = true
        showCauses = true
        showStackTraces = true

        showStandardStreams = false
    }
}
