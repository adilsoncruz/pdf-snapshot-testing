package com.github.adilsoncruz.pdfsnapshottesting.models

import java.awt.image.BufferedImage

public data class DiffResult(
    val image: BufferedImage,
    val matches: Boolean,
)
