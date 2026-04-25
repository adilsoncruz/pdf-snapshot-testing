package com.github.adilsoncruz.pdfsnapshottesting

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class SnapshotContextKotestTest : FunSpec({
    test("should manage current test id in thread local") {
        CurrentTest.set("test-123")

        CurrentTest.get() shouldBe "test-123"

        CurrentTest.clear()
        CurrentTest.get().shouldBeNull()
    }

    test("should store and remove execution contexts by unique id") {
        val context =
            SnapshotExecutionContext(
                testName = "sample-test",
                className = "SampleClass",
                uniqueId = "uid-1",
            )

        SnapshotContextManager.put("uid-1", context)
        SnapshotContextManager.get("uid-1") shouldBe context

        SnapshotContextManager.remove("uid-1")
        SnapshotContextManager.get("uid-1").shouldBeNull()
    }
})
