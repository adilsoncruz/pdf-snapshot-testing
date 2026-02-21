package com.github.adilsoncruz.pdfsnapshottesting.models

public data class PdfRegion(
    val x: Float,
    val yFromTop: Float,
    val width: Float,
    val height: Float,
) {
    public companion object {
        public fun fullPage(
            pageWidth: Float,
            pageHeight: Float,
        ): PdfRegion =
            PdfRegion(
                x = 0f,
                yFromTop = 0f,
                width = pageWidth,
                height = pageHeight,
            )
    }
}
