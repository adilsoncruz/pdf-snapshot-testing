package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import java.lang.reflect.InvocationTargetException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SnapshotReportRendererTest {
    private val renderer = SnapshotReportRenderer()

    @Test
    fun `generates report with passed and failed sections`() {
        val groupedReports =
            linkedMapOf(
                "passed" to listOf(sampleReport(matches = true, className = "PassedClass", testName = "test passed")),
                "failed" to listOf(sampleReport(matches = false, className = "FailedClass", testName = "test failed")),
            )

        val html = renderer.generateReport(groupedReports)

        assertTrue(html.contains("PassedClass"))
        assertTrue(html.contains("FailedClass"))
        assertTrue(html.contains("#4CAF50"))
        assertTrue(html.contains("#E53935"))
        assertTrue(html.contains("difference ratio: 0.25"))
        assertTrue(html.contains("tolerance percentage: 5.0%"))
    }

    @Test
    fun `throws when template does not exist`() {
        val method =
            SnapshotReportRenderer::class.java
                .getDeclaredMethod("loadTemplate", String::class.java)
                .apply { isAccessible = true }

        val error =
            assertThrows<InvocationTargetException> {
                method.invoke(renderer, "does-not-exist.html")
            }

        assertTrue(error.targetException is RuntimeException)
        assertEquals("template.html not found in resources", error.targetException.message)
    }

    private fun sampleReport(matches: Boolean, className: String, testName: String): SnapshotReport =
        SnapshotReport(
            testName = testName,
            className = className,
            expectedPageSize = 1,
            generatedPageSize = 1,
            results =
                listOf(
                    SnapshotResult(
                        name = "page-1",
                        expected = "expected.png",
                        generated = "generated.png",
                        diff = "diff.png",
                        matches = matches,
                        differenceRatio = 0.25f,
                        tolerancePercentage = 5.0f,
                    ),
                ),
        )
}
