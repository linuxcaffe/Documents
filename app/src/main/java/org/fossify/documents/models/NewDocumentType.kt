package org.fossify.documents.models

enum class NewDocumentType(
    val mimeType: String,
    val extension: String,
    val kind: DocumentKind,
) {
    TEXT("text/plain", "txt", DocumentKind.TEXT),
    MARKDOWN("text/markdown", "md", DocumentKind.MARKDOWN),
}
