package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class SnapshotResultFileWriterTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `writes snapshot report file with sanitized class and test name`() {
        val context = SnapshotExecutionContext(testName = "renders pdf", className = "InvoiceTest")
        val report = sampleReport("page-1")
        val expectedOutput = File("build/pdf-snapshots/raw/InvoiceTest-renders_pdf.json")
        expectedOutput.delete()

        try {
            val writtenFile = SnapshotResultFileWriter.write(context, report)

            assertEquals(expectedOutput, writtenFile)
            assertTrue(writtenFile.exists())

            val persistedReport = SnapshotJsonSerializer.deserialize(writtenFile.readText())
            assertEquals("renders pdf", persistedReport.testName)
            assertEquals("InvoiceTest", persistedReport.className)
            assertEquals(report.results, persistedReport.results)
            assertEquals(report.expectedPageSize, persistedReport.expectedPageSize)
            assertEquals(report.generatedPageSize, persistedReport.generatedPageSize)
        } finally {
            expectedOutput.delete()
        }
    }

    @Test
    fun `preserves multiple tests in the same class and multiple assertions in the same test`() {
        val config = SnapshotConfig(snapshotsResultDir = tempDir.toString())
        val firstTest = SnapshotExecutionContext(testName = "renders header", className = "InvoiceTest")
        val secondTest = SnapshotExecutionContext(testName = "renders footer", className = "InvoiceTest")

        val file1 = SnapshotResultFileWriter.write(firstTest, sampleReport("header-1"), config)
        val file2 = SnapshotResultFileWriter.write(firstTest, sampleReport("header-2"), config)
        val file3 = SnapshotResultFileWriter.write(firstTest, sampleReport("header-3"), config)
        val file4 = SnapshotResultFileWriter.write(secondTest, sampleReport("footer-1"), config)

        assertEquals("InvoiceTest-renders_header.json", file1.name)
        assertEquals("InvoiceTest-renders_header-2.json", file2.name)
        assertEquals("InvoiceTest-renders_header-3.json", file3.name)
        assertEquals("InvoiceTest-renders_footer.json", file4.name)

        val indexFile = generateSnapshotHtmlReport(config = config, cleanRawFiles = false)
        assertTrue(indexFile!!.exists())
        val html = indexFile.readText()
        assertTrue(html.contains("renders header"))
        assertTrue(html.contains("renders footer"))
        assertTrue(html.contains("header-1"))
        assertTrue(html.contains("header-2"))
        assertTrue(html.contains("header-3"))
        assertTrue(html.contains("footer-1"))
    }

    private fun sampleReport(resultName: String): SnapshotReport =
        SnapshotReport(
            testName = "ignored test",
            className = "ignored class",
            expectedPageSize = 2,
            generatedPageSize = 2,
            results =
                listOf(
                    SnapshotResult(
                        name = resultName,
                        expected = "expected.png",
                        generated = "generated.png",
                        diff = "diff.png",
                        matches = true,
                        differenceRatio = 0.0f,
                        tolerancePercentage = 2.5f,
                    ),
                ),
        )
}
