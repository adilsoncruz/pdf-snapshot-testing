package com.github.adilsoncruz.pdfsnapshottesting.kotest

import com.github.adilsoncruz.pdfsnapshottesting.SnapshotExecutionContext
import java.util.concurrent.ConcurrentHashMap

public object KotestSnapshotContextManager {
    private val contexts =
        ConcurrentHashMap<String, SnapshotExecutionContext>()

    public fun put(
        uniqueId: String,
        context: SnapshotExecutionContext,
    ) {
        contexts[uniqueId] = context
    }

    public fun get(uniqueId: String): SnapshotExecutionContext? {
        return contexts[uniqueId]
    }

    public fun remove(uniqueId: String) {
        contexts.remove(uniqueId)
    }
}
