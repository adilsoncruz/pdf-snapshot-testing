package com.github.adilsoncruz.pdfsnapshottesting

import com.github.adilsoncruz.pdfsnapshottesting.models.PdfRegion

public class PdfTextAssertion(
    private val pdfBytes: ByteArray,
    private val engine: PdfSnapshotEngine = PdfSnapshotEngine(),
) {
    private var pageIndex: Int = 0

    public fun page(index: Int): PdfTextAssertion {
        require(index >= 0) { "Page index must be >= 0" }
        pageIndex = index
        return this
    }

    public fun region(
        x: Number,
        yFromTop: Number,
        width: Number,
        height: Number,
    ): PdfTextRegionAssertion =
        PdfTextRegionAssertion(
            pdfBytes = pdfBytes,
            pageIndex = pageIndex,
            region =
                PdfRegion(
                    x = x.toFloat(),
                    yFromTop = yFromTop.toFloat(),
                    width = width.toFloat(),
                    height = height.toFloat(),
                ),
            engine = engine,
        )
}

public class PdfTextRegionAssertion(
    private val pdfBytes: ByteArray,
    private val pageIndex: Int,
    private val region: PdfRegion,
    private val engine: PdfSnapshotEngine,
) {
    public fun containsText(expectedText: String) {
        val actualText = engine.extractText(pdfBytes, pageIndex, region)

        if (!actualText.contains(expectedText)) {
            throw AssertionError(
                "Expected PDF region to contain text '$expectedText', but extracted '$actualText'.",
            )
        }
    }
}
