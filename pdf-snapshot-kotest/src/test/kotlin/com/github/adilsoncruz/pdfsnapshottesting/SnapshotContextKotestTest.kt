package com.github.adilsoncruz.pdfsnapshottesting.kotest

import com.github.adilsoncruz.pdfsnapshottesting.SnapshotExecutionContext
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class SnapshotContextKotestTest : FunSpec({
    test("should manage current test id in thread local") {
        CurrentKotestTest.set("test-123")

        CurrentKotestTest.get() shouldBe "test-123"

        CurrentKotestTest.clear()
        CurrentKotestTest.get().shouldBeNull()
    }

    test("should store and remove execution contexts by unique id") {
        val context =
            SnapshotExecutionContext(
                testName = "sample-test",
                className = "SampleClass",
                uniqueId = "uid-1",
            )

        KotestSnapshotContextManager.put("uid-1", context)
        KotestSnapshotContextManager.get("uid-1") shouldBe context

        KotestSnapshotContextManager.remove("uid-1")
        KotestSnapshotContextManager.get("uid-1").shouldBeNull()
    }
})
