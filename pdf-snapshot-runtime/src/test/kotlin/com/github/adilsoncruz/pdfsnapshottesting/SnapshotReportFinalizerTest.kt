package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class SnapshotReportFinalizerTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `generateSnapshotHtmlReport returns null when raw directory is empty or missing`() {
        val config = SnapshotConfig(snapshotsResultDir = tempDir.resolve("missing").toString())

        val result = generateSnapshotHtmlReport(config = config)

        assertNull(result)
    }

    @Test
    fun `generateSnapshotHtmlReport generates index html and cleans raw files when requested`() {
        val resultDir = tempDir.resolve("report-test").toFile()
        val rawDir = File(resultDir, "raw").apply { mkdirs() }
        val config = SnapshotConfig(snapshotsResultDir = resultDir.path)

        val sampleReport =
            SnapshotReport(
                className = "OrderTest",
                testName = "renders order",
                expectedPageSize = 1,
                generatedPageSize = 1,
                results =
                    listOf(
                        SnapshotResult(
                            name = "Page 1",
                            expected = "exp",
                            generated = "gen",
                            diff = "diff",
                            matches = true,
                            differenceRatio = 0f,
                            tolerancePercentage = 0.01f,
                        ),
                    ),
            )

        val rawFile = File(rawDir, "OrderTest.json")
        rawFile.writeText(SnapshotJsonSerializer.serialize(sampleReport))

        val indexFile = generateSnapshotHtmlReport(config = config, cleanRawFiles = true)

        assertNotNull(indexFile)
        assertTrue(indexFile!!.exists())
        assertEquals("index.html", indexFile.name)
        assertTrue(indexFile.readText().contains("OrderTest"))
        assertFalse(rawDir.exists())
    }

    @Test
    fun `generateSnapshotHtmlReport keeps raw files when cleanRawFiles is false`() {
        val resultDir = tempDir.resolve("keep-raw").toFile()
        val rawDir = File(resultDir, "raw").apply { mkdirs() }
        val config = SnapshotConfig(snapshotsResultDir = resultDir.path)

        val sampleReport =
            SnapshotReport(
                className = "InvoiceTest",
                testName = "renders invoice",
                expectedPageSize = 1,
                generatedPageSize = 1,
                results =
                    listOf(
                        SnapshotResult(
                            name = "Page 1",
                            expected = "exp",
                            generated = "gen",
                            diff = "diff",
                            matches = true,
                            differenceRatio = 0f,
                            tolerancePercentage = 0.01f,
                        ),
                    ),
            )

        File(rawDir, "InvoiceTest.json").writeText(SnapshotJsonSerializer.serialize(sampleReport))

        val indexFile = generateSnapshotHtmlReport(config = config, cleanRawFiles = false)

        assertNotNull(indexFile)
        assertTrue(indexFile!!.exists())
        assertTrue(rawDir.exists())
    }
}
