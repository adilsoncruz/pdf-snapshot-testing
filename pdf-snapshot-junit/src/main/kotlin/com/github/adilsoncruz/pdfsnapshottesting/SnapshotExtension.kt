package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.File

public class SnapshotExtension : BeforeEachCallback, AfterEachCallback, BeforeAllCallback {
    public override fun beforeAll(context: ExtensionContext) {
        context.root.getStore(ExtensionContext.Namespace.GLOBAL)
            .getOrComputeIfAbsent("snapshot-finalizer") {
                SnapshotFinalizer()
            }
    }

    public class SnapshotFinalizer : ExtensionContext.Store.CloseableResource {
        override fun close() {
            val config = SnapshotConfig()
            val rawDir = File(config.snapshotsResultDir, "raw")

            val reports =
                rawDir.listFiles()
                    ?.map {
                        SnapshotJsonSerializer.deserialize(it.readText())
                    }
                    ?: emptyList()

            if (reports.isNotEmpty()) {
                val indexFile = File(config.snapshotsResultDir, "index.html")
                val grouped = reports.groupBy { it.className }

                val renderer = SnapshotReportRenderer()

                val html =
                    // renderer.generateConsolidatedHtml(
                    renderer.generateReport(
                        grouped,
                    )
                // val html = renderer.generateBindingBox(grouped)

                indexFile.writeText(html)
                rawDir.deleteRecursively()
                println()
                println("PDF Snapshot Report:")
                println("file://${indexFile.absolutePath}")
                println()
            }
        }
    }

    override fun beforeEach(context: ExtensionContext) {
        CurrentTest.set(context.uniqueId)

        val executionContext =
            SnapshotExecutionContext(
                testName = context.displayName,
                className = context.requiredTestClass.simpleName,
                uniqueId = context.uniqueId,
            )
        SnapshotContextManager.put(context.uniqueId, executionContext)
    }

    override fun afterEach(context: ExtensionContext) {
        CurrentTest.clear()
        SnapshotContextManager.remove(context.uniqueId)
    }

    public companion object {
        public fun getContext(context: ExtensionContext): SnapshotExecutionContext {
            return context.getStore(
                ExtensionContext.Namespace.create(SnapshotExtension::class.java),
            ).get("snapshotContext", SnapshotExecutionContext::class.java)
                ?: error("SnapshotExtension not registered.")
        }
    }
}
