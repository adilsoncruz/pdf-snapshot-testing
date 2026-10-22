package com.github.adilsoncruz.pdfsnapshottesting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SnapshotRuntimeContextUnitTest {
    @Test
    fun `CurrentTest should set, get and clear unique id in thread local`() {
        assertNull(CurrentTest.get())

        CurrentTest.set("test-123")
        assertEquals("test-123", CurrentTest.get())

        CurrentTest.clear()
        assertNull(CurrentTest.get())
    }

    @Test
    fun `SnapshotContextManager should store, retrieve and remove context`() {
        val context = SnapshotExecutionContext("myTest", "MyClass", "unique-1")

        SnapshotContextManager.put("unique-1", context)
        assertEquals(context, SnapshotContextManager.get("unique-1"))

        SnapshotContextManager.remove("unique-1")
        assertNull(SnapshotContextManager.get("unique-1"))
    }
}
