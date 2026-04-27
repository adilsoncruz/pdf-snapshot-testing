package com.github.adilsoncruz.pdfsnapshottesting.models

public data class SnapshotResult(
    val name: String,
    val expected: String,
    val generated: String,
    val diff: String,
    val matches: Boolean,
    val differenceRatio: Float,
    val tolerancePercentage: Float,
    val addedBoxes: List<BoundingBox>? = emptyList(),
    val removedBoxes: List<BoundingBox>? = emptyList(),
    val changedBoxes: List<BoundingBox>? = emptyList(),
)
