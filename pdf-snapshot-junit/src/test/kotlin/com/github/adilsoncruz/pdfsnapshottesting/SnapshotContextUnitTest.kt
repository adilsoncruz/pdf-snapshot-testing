package com.github.adilsoncruz.pdfsnapshottesting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SnapshotContextUnitTest {
    @Test
    fun `should manage current test id in thread local`() {
        CurrentTest.set("test-123")

        assertEquals("test-123", CurrentTest.get())

        CurrentTest.clear()
        assertNull(CurrentTest.get())
    }

    @Test
    fun `should store and remove execution contexts by unique id`() {
        val context =
            SnapshotExecutionContext(
                testName = "sample-test",
                className = "SampleClass",
                uniqueId = "uid-1",
            )

        SnapshotContextManager.put("uid-1", context)
        assertEquals(context, SnapshotContextManager.get("uid-1"))

        SnapshotContextManager.remove("uid-1")
        assertNull(SnapshotContextManager.get("uid-1"))
    }
}
