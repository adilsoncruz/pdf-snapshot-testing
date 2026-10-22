package com.github.adilsoncruz.pdfsnapshottesting.kotest

import com.github.adilsoncruz.pdfsnapshottesting.CurrentTest
import com.github.adilsoncruz.pdfsnapshottesting.SnapshotContextManager
import com.github.adilsoncruz.pdfsnapshottesting.SnapshotJsonSerializer
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestResult
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

class SnapshotKotestExtensionTest : FunSpec({
    test("should manage context on beforeTest and afterTest") {
        SnapshotKotestExtension.beforeTest(this.testCase)

        val uniqueId = this.testCase.descriptor.id.value
        CurrentTest.get() shouldBe uniqueId

        val stored = SnapshotContextManager.get(uniqueId)
        stored?.testName shouldBe this.testCase.name.testName

        SnapshotKotestExtension.afterTest(this.testCase, TestResult.Success(0.milliseconds))

        CurrentTest.get().shouldBeNull()
        SnapshotContextManager.get(uniqueId).shouldBeNull()
    }

    test("afterProject should generate report and log link when files exist") {
        val baseDir = File("build/pdf-snapshots")
        val rawDir = File(baseDir, "raw").apply { mkdirs() }

        val sampleReport =
            SnapshotReport(
                testName = "kotest project test",
                className = "KotestSpec",
                expectedPageSize = 1,
                generatedPageSize = 1,
                results = listOf(SnapshotResult("1", "e", "g", "d", true, 0f, 0.1f)),
            )

        File(rawDir, "KotestSpec.json").writeText(SnapshotJsonSerializer.serialize(sampleReport))

        SnapshotKotestExtension.afterProject()

        File(baseDir, "index.html").exists() shouldBe true
        rawDir.exists() shouldBe false

        baseDir.deleteRecursively()
    }

    test("afterProject should do nothing when raw directory is empty") {
        val baseDir = File("build/pdf-snapshots")
        baseDir.deleteRecursively()

        SnapshotKotestExtension.afterProject()

        File(baseDir, "index.html").exists() shouldBe false
    }
})
