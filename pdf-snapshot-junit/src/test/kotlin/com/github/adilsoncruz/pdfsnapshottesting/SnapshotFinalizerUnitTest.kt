package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotPageResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class SnapshotFinalizerUnitTest {
    @Test
    fun `should generate consolidated html report and remove raw files`() {
        val resultDir = File("build/pdf-snapshots")
        val rawDir = File(resultDir, "raw")

        resultDir.deleteRecursively()
        rawDir.mkdirs()

        val report =
            SnapshotReport(
                testName = "sample-test",
                className = "SampleClass",
                results = listOf(SnapshotPageResult(0, "e", "g", "d", true, 0f, 0.1f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        File(rawDir, "SampleClass.json").writeText(SnapshotJsonSerializer.serialize(report))

        SnapshotExtension.SnapshotFinalizer().close()

        val indexFile = File(resultDir, "index.html")
        assertTrue(indexFile.exists())
        assertFalse(rawDir.exists())

        resultDir.deleteRecursively()
    }
}
