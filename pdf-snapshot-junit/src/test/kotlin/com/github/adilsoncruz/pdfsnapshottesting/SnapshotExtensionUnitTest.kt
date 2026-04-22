package com.github.adilsoncruz.pdfsnapshottesting

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.function.Function

class SnapshotExtensionUnitTest {
    private val extension = SnapshotExtension()

    @Test
    fun `beforeAll should register snapshot finalizer in global store`() {
        val context = mock<ExtensionContext>()
        val root = mock<ExtensionContext>()
        val store = mock<ExtensionContext.Store>()

        whenever(context.root).thenReturn(root)
        whenever(root.getStore(ExtensionContext.Namespace.GLOBAL)).thenReturn(store)
        whenever(
            store.getOrComputeIfAbsent(
                eq("snapshot-finalizer"),
                any<Function<Any, Any>>(),
            ),
        ).thenAnswer { invocation ->
            val generator = invocation.arguments[1] as Function<Any, Any>
            generator.apply("snapshot-finalizer")
        }

        extension.beforeAll(context)

        verify(root).getStore(ExtensionContext.Namespace.GLOBAL)
        verify(store).getOrComputeIfAbsent(eq("snapshot-finalizer"), any<Function<Any, Any>>())
    }

    @Test
    fun `beforeEach and afterEach should manage current test and context map`() {
        val context = mock<ExtensionContext>()
        whenever(context.uniqueId).thenReturn("uid-123")
        whenever(context.displayName).thenReturn("my test")
        whenever(context.requiredTestClass).thenReturn(SnapshotExtensionUnitTest::class.java)

        extension.beforeEach(context)

        assertEquals("uid-123", CurrentTest.get())
        assertNotNull(SnapshotContextManager.get("uid-123"))

        extension.afterEach(context)

        assertEquals(null, CurrentTest.get())
        assertEquals(null, SnapshotContextManager.get("uid-123"))
    }

    @Test
    fun `getContext should return stored snapshot context`() {
        val context = mock<ExtensionContext>()
        val store = mock<ExtensionContext.Store>()
        val expected =
            SnapshotExecutionContext(
                testName = "name",
                className = "ClassName",
                uniqueId = "uid-1",
            )

        whenever(context.getStore(ExtensionContext.Namespace.create(SnapshotExtension::class.java))).thenReturn(store)
        whenever(store.get("snapshotContext", SnapshotExecutionContext::class.java)).thenReturn(expected)

        val actual = SnapshotExtension.getContext(context)

        assertEquals(expected, actual)
    }

    @Test
    fun `getContext should fail when extension context was not registered`() {
        val context = mock<ExtensionContext>()
        val store = mock<ExtensionContext.Store>()

        whenever(context.getStore(ExtensionContext.Namespace.create(SnapshotExtension::class.java))).thenReturn(store)
        whenever(store.get("snapshotContext", SnapshotExecutionContext::class.java)).thenReturn(null)

        assertThrows(IllegalStateException::class.java) {
            SnapshotExtension.getContext(context)
        }
    }
}
