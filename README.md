# PDF Snapshot Testing (JVM)

Visual snapshot testing for PDFs on the JVM.
Test **what users see**, not unstable PDF internals.

---

## Why this exists

Testing PDFs is hard.

Comparing generated PDFs directly almost always fails because PDFs are not deterministic:
- metadata changes (timestamps, IDs)
- internal object ordering
- font embedding differences
- compression variations

Even when nothing changes visually, binary or structural comparisons break.

What usually matters is:

Does this PDF still render the same?

This library answers that question.

---

## What this library does

- Renders PDFs to images using Apache PDFBox
- Performs visual snapshot comparisons
- Allows region-based assertions
- Supports multiple snapshots from the same PDF
- Provides debug overlays for coordinate validation
- Works with any PDF generator
- Designed for backend JVM testing

---

## What this library does NOT do

- Does not generate PDFs
- Does not compare PDFs byte-by-byte
- Does not understand semantic layout
- Does not replace business-logic assertions
- Does not depend on OpenPDF, iText, Jasper, etc.

This library tests final visual output only.

## License

MIT
