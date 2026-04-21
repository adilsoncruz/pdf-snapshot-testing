package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

class SnapshotJsonSerializerTest {
    @Test
    fun `serializes and deserializes snapshot report`() {
        val report = sampleReport()

        val json = SnapshotJsonSerializer.serialize(report)
        val deserialized = SnapshotJsonSerializer.deserialize(json)

        assertEquals(report, deserialized)
        assertFalse(deserialized.hasFailures)
    }

    @Test
    fun `ignores unknown fields during deserialization`() {
        val json =
            """
            {
              "results": [
                {
                  "name": "page-1",
                  "expected": "expected.png",
                  "generated": "generated.png",
                  "diff": "diff.png",
                  "matches": true,
                  "differenceRatio": 0.0,
                  "tolerancePercentage": 0.5,
                  "unknownResultField": "ignored"
                }
              ],
              "testName": "render invoice",
              "className": "InvoiceTest",
              "expectedPageSize": 1,
              "generatedPageSize": 1,
              "unknownRootField": "ignored"
            }
            """.trimIndent()

        val report = SnapshotJsonSerializer.deserialize(json)

        assertEquals("render invoice", report.testName)
        assertEquals("InvoiceTest", report.className)
        assertEquals(1, report.results.size)
    }

    private fun sampleReport(): SnapshotReport =
        SnapshotReport(
            testName = "render invoice",
            className = "InvoiceTest",
            expectedPageSize = 1,
            generatedPageSize = 1,
            results =
                listOf(
                    SnapshotResult(
                        name = "page-1",
                        expected = "expected.png",
                        generated = "generated.png",
                        diff = "diff.png",
                        matches = true,
                        differenceRatio = 0.0f,
                        tolerancePercentage = 0.5f,
                    ),
                ),
        )
}
