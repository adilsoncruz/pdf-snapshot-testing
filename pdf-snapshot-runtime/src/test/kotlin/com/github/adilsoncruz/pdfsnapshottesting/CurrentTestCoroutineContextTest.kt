package com.github.adilsoncruz.pdfsnapshottesting

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CurrentTestCoroutineContextTest {
    @Test
    fun `asContextElement should propagate uniqueId across dispatcher thread switches`() =
        runBlocking {
            val uniqueId = "test-coroutine-id"

            withContext(CurrentTestCoroutineContext.asContextElement(uniqueId)) {
                val childJob =
                    async(Dispatchers.Default) {
                        CurrentTest.get()
                    }
                val resolvedId = childJob.await()
                assertEquals(uniqueId, resolvedId)
            }

            assertNull(CurrentTestCoroutineContext.get())
        }
}
