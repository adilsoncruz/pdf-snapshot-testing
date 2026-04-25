package com.github.adilsoncruz.pdfsnapshottesting

public data class SnapshotExecutionContext(
    val testName: String,
    val className: String,
    val uniqueId: String = "$className#$testName",
)
