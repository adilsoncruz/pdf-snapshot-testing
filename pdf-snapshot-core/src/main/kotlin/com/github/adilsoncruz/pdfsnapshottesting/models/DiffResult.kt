package com.github.adilsoncruz.pdfsnapshottesting.models

import java.awt.image.BufferedImage

// public data class DiffResult(
//    val image: BufferedImage,
//    val matches: Boolean,
// )

public data class DiffResult(
    val diffImage: BufferedImage,
    val expectedHighlighted: BufferedImage,
    val generatedHighlighted: BufferedImage,
    val matches: Boolean,
    val differenceRatio: Float,
    val tolerancePercentage: Float,
    val addedBoxes: List<BoundingBox>? = emptyList(),
    val removedBoxes: List<BoundingBox>? = emptyList(),
    val changedBoxes: List<BoundingBox>? = emptyList(),
)
