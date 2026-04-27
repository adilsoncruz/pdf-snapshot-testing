package com.github.adilsoncruz.pdfsnapshottesting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnapshotExecutionContextTest {
    @Test
    fun `uses default unique id when one is not provided`() {
        val context =
            SnapshotExecutionContext(
                testName = "rendersPdf",
                className = "InvoiceTest",
            )

        assertEquals("InvoiceTest#rendersPdf", context.uniqueId)
    }

    @Test
    fun `keeps explicit unique id when provided`() {
        val context =
            SnapshotExecutionContext(
                testName = "rendersPdf",
                className = "InvoiceTest",
                uniqueId = "custom-id",
            )

        assertEquals("custom-id", context.uniqueId)
    }
}
