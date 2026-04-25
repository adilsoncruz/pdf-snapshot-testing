package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.io.File

class SnapshotFinalizerKotestTest : FunSpec({
    test("should generate consolidated html report and remove raw files") {
        val resultDir = File("build/pdf-snapshots")
        val rawDir = File(resultDir, "raw")

        resultDir.deleteRecursively()
        rawDir.mkdirs()

        val report =
            SnapshotReport(
                testName = "sample-test",
                className = "SampleClass",
                results = listOf(SnapshotResult("0", "e", "g", "d", true, 0f, 0.1f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        File(rawDir, "SampleClass.json").writeText(SnapshotJsonSerializer.serialize(report))

        generateSnapshotHtmlReport(cleanRawFiles = true)

        File(resultDir, "index.html").exists() shouldBe true
        rawDir.exists() shouldBe false

        resultDir.deleteRecursively()
    }
})
