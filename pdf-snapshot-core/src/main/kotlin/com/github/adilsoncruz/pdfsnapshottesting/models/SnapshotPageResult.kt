package com.github.adilsoncruz.pdfsnapshottesting.models

public data class SnapshotPageResult(
    val pageIndex: Int,
    val expected: String,
    val generated: String,
    val diff: String,
    val matches: Boolean,
)
