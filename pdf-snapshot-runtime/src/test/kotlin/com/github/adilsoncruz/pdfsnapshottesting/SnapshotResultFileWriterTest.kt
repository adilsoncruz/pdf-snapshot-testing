package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class SnapshotResultFileWriterTest {
    @Test
    fun `writes snapshot report file with context metadata`() {
        val context = SnapshotExecutionContext(testName = "renders pdf", className = "InvoiceTest")
        val report =
            SnapshotReport(
                testName = "ignored test",
                className = "ignored class",
                expectedPageSize = 2,
                generatedPageSize = 2,
                results =
                    listOf(
                        SnapshotResult(
                            name = "page-1",
                            expected = "expected.png",
                            generated = "generated.png",
                            diff = "diff.png",
                            matches = true,
                            differenceRatio = 0.0f,
                            tolerancePercentage = 2.5f,
                        ),
                    ),
            )

        SnapshotResultFileWriter.write(context, report)

        val outputFile = File("build/pdf-snapshots/raw/InvoiceTest.json")
        assertTrue(outputFile.exists())

        val persistedReport = SnapshotJsonSerializer.deserialize(outputFile.readText())
        assertEquals("renders pdf", persistedReport.testName)
        assertEquals("InvoiceTest", persistedReport.className)
        assertEquals(report.results, persistedReport.results)
        assertEquals(report.expectedPageSize, persistedReport.expectedPageSize)
        assertEquals(report.generatedPageSize, persistedReport.generatedPageSize)
    }
}
