package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class SnapshotReportFinalizerLifecycleTest {
    @Test
    fun `finalizeReport should assert report without runtime context`() {
        val report =
            SnapshotReport(
                results = listOf(SnapshotResult("p1", "e", "g", "d", true, 0f, 0f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        assertDoesNotThrow {
            SnapshotReportFinalizerLifecycle.finalizeReport(report)
        }
    }

    @Test
    fun `finalizeReport should throw for mismatching report`() {
        val report =
            SnapshotReport(
                results = listOf(SnapshotResult("p1", "e", "g", "d", false, 0.3f, 0f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        assertThrows(AssertionError::class.java) {
            SnapshotReportFinalizerLifecycle.finalizeReport(report)
        }
    }

    @Test
    fun `finalizeReport should write runtime report when CurrentTest is present`() {
        val output = File("build/pdf-snapshots/raw/LifecycleTest-writes_report.json")
        output.delete()

        val uniqueId = "uid-lifecycle"
        CurrentTest.set(uniqueId)
        SnapshotContextManager.put(
            uniqueId,
            SnapshotExecutionContext("writes report", "LifecycleTest", uniqueId),
        )

        try {
            val report =
                SnapshotReport(
                    results = listOf(SnapshotResult("p1", "e", "g", "d", true, 0f, 0f)),
                    expectedPageSize = 1,
                    generatedPageSize = 1,
                )
            SnapshotReportFinalizerLifecycle.finalizeReport(report)
            assertTrue(output.exists())
        } finally {
            CurrentTest.clear()
            SnapshotContextManager.remove(uniqueId)
            output.delete()
        }
    }
}
