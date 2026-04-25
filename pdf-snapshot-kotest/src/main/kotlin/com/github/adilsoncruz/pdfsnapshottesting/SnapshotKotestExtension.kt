package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.listeners.BeforeTestListener
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult

public object SnapshotKotestExtension : BeforeTestListener, AfterTestListener, AfterProjectListener {
    override suspend fun beforeTest(testCase: TestCase) {
        val uniqueId = testCase.descriptor.id.value
        CurrentTest.set(uniqueId)

        SnapshotContextManager.put(
            uniqueId,
            SnapshotExecutionContext(
                testName = testCase.name.testName,
                className = testCase.spec::class.simpleName ?: "UnknownSpec",
                uniqueId = uniqueId,
            ),
        )
    }

    override suspend fun afterTest(
        testCase: TestCase,
        result: TestResult,
    ) {
        val uniqueId = testCase.descriptor.id.value
        CurrentTest.clear()
        SnapshotContextManager.remove(uniqueId)
    }

    override suspend fun afterProject() {
        val indexFile = generateSnapshotHtmlReport(config = SnapshotConfig(), cleanRawFiles = true)

        if (indexFile != null) {
            println()
            println("PDF Snapshot Report:")
            println("file://${indexFile.absolutePath}")
            println()
        }
    }
}
