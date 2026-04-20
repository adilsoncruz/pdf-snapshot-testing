package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotResult
import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotReport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import java.io.File

@EnablePdfSnapshots
class SnapshotExtensionSampleSpec {
    @Test
    fun `should execute snapshot assertion through junit extension lifecycle`() {
        val uniqueId = checkNotNull(CurrentTest.get())
        val context = checkNotNull(SnapshotContextManager.get(uniqueId))

        val report =
            SnapshotReport(
                testName = context.testName,
                className = context.className,
                results = listOf(SnapshotResult("0", "e", "g", "d", true, 0f, 0.1f)),
                expectedPageSize = 1,
                generatedPageSize = 1,
            )

        SnapshotResultFileWriter.write(context, report)
    }
}

class PdfSnapshotJUnitIntegrationTest {
    @Test
    fun `should create runtime artifacts when running test class with snapshot extension`() {
        val resultDir = File("build/pdf-snapshots")
        resultDir.deleteRecursively()

        val request =
            LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(SnapshotExtensionSampleSpec::class.java))
                .build()

        val listener = SummaryGeneratingListener()

        LauncherFactory.openSession().use { session ->
            val launcher = session.launcher
            launcher.registerTestExecutionListeners(listener)
            launcher.execute(request)
        }

        val summary = listener.summary
        assertEquals(1, summary.testsSucceededCount)
        assertEquals(0, summary.testsFailedCount)

        val indexFile = File(resultDir, "index.html")
        assertTrue(indexFile.exists())
        assertFalse(File(resultDir, "raw").exists())

        resultDir.deleteRecursively()
    }
}
