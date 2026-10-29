package org.fossify.documents.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.fossify.documents.models.DocumentKind

/** The in-app viewer or editor for [kind], or null when the app can't open it. */
internal fun Context.documentViewerIntent(uri: Uri, kind: DocumentKind, prepared: Boolean = true): Intent? {
    return when (kind) {
        DocumentKind.PDF -> Intent(this, PDFViewerActivity::class.java).apply { data = uri }
        DocumentKind.TEXT,
        DocumentKind.MARKDOWN -> TextDocumentActivity.newIntent(this, uri, kind, prepared)

        DocumentKind.DOCX,
        DocumentKind.CSV,
        DocumentKind.HTML -> StructuredDocumentActivity.newIntent(this, uri, kind, prepared)

        DocumentKind.OTHER -> null
    }
}
