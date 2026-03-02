package com.github.adilsoncruz.pdfsnapshottesting.models

import java.awt.Rectangle

public data class SnapshotPageResult(
    val pageIndex: Int,
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
