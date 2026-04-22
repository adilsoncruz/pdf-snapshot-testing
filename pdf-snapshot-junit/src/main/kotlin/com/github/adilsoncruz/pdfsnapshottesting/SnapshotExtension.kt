package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.SnapshotConfig
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

public class SnapshotExtension : BeforeEachCallback, AfterEachCallback, BeforeAllCallback {
    public override fun beforeAll(context: ExtensionContext) {
        context.root.getStore(ExtensionContext.Namespace.GLOBAL)
            .getOrComputeIfAbsent("snapshot-finalizer") {
                SnapshotFinalizer()
            }
    }

    public class SnapshotFinalizer : ExtensionContext.Store.CloseableResource {
        override fun close() {
            val indexFile = generateSnapshotHtmlReport(config = SnapshotConfig(), cleanRawFiles = true)

            if (indexFile != null) {
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
