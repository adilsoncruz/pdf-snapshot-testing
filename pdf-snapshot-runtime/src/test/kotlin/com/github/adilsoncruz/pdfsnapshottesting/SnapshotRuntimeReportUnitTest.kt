package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotPageResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class SnapshotRuntimeReportUnitTest {
    @Test
    fun `should create one raw report file per test unique id`() {
        val rawDir = File("build/pdf-snapshots/raw")
        rawDir.deleteRecursively()

        val report =
            SnapshotReport(
                testName = "test-a",
                className = "InvoiceSpec",
                results = listOf(SnapshotPageResult("page-1", "e", "g", "d", true, 0f, 0.01f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        SnapshotResultFileWriter.write(
            SnapshotExecutionContext(testName = "test-a", className = "InvoiceSpec", uniqueId = "[engine:junit-jupiter]/[class:InvoiceSpec]/[method:testA()]"),
            report,
        )
        SnapshotResultFileWriter.write(
            SnapshotExecutionContext(testName = "test-b", className = "InvoiceSpec", uniqueId = "[engine:junit-jupiter]/[class:InvoiceSpec]/[method:testB()]"),
            report.copy(testName = "test-b"),
        )

        val createdFiles = rawDir.listFiles()?.filter { it.extension == "json" } ?: emptyList()
        assertEquals(2, createdFiles.size)

        rawDir.deleteRecursively()
    }

    @Test
    fun `should render empty comparison state without crashing`() {
        val renderer = SnapshotReportRenderer()
        val grouped =
            mapOf(
                "InvoiceSpec" to
                    listOf(
                        SnapshotReport(
                            testName = "baseline",
                            className = "InvoiceSpec",
                            results = emptyList(),
                            expectedPageSize = 1,
                            generatedPageSize = 1,
                        ),
                    ),
            )

        val html = renderer.generateReport(grouped)

        assertTrue(html.contains("No diff data available"))
        assertTrue(html.contains("No snapshot pages were compared"))
    }
}
