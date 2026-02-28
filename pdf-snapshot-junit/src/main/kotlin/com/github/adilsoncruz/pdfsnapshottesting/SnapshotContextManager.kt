package com.github.adilsoncruz.pdfsnapshottesting

import java.util.concurrent.ConcurrentHashMap

public object SnapshotContextManager {
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
