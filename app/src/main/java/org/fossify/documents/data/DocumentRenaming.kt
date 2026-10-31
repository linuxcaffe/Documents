package org.fossify.documents.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.FileNotFoundException

/** Renames the file in its folder; returns its new URI (the same one when the provider keeps it), or null. */
internal fun Context.renameDocumentFile(uri: Uri, name: String): Uri? {
    return try {
        DocumentsContract.renameDocument(contentResolver, uri, name) ?: uri
    } catch (_: FileNotFoundException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: IllegalStateException) {
        null
    } catch (_: UnsupportedOperationException) {
        null
    }
}
