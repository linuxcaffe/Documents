package org.fossify.documents.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.FileNotFoundException

/** Whether the document's provider lets this app delete it (files in an opened folder usually can be). */
internal fun Context.canDeleteDocument(uri: Uri): Boolean {
    val projection = arrayOf(DocumentsContract.Document.COLUMN_FLAGS)
    return try {
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            cursor.moveToFirst() &&
                    (cursor.getInt(0) and DocumentsContract.Document.FLAG_SUPPORTS_DELETE) != 0
        } ?: false
    } catch (_: SecurityException) {
        false
    } catch (_: IllegalArgumentException) {
        false
    }
}

/** Deletes the file itself, not just the app's entry for it. False when the provider refused. */
internal fun Context.deleteDocumentFile(uri: Uri): Boolean {
    return try {
        DocumentsContract.deleteDocument(contentResolver, uri)
    } catch (_: FileNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    } catch (_: IllegalArgumentException) {
        false
    } catch (_: UnsupportedOperationException) {
        false
    }
}
