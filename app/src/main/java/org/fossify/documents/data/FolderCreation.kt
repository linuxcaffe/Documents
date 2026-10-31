package org.fossify.documents.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.FileNotFoundException

/** Creates a folder named [name] inside the browsed folder [folderUri]; its URI, or null when refused. */
internal fun Context.createChildFolder(folderUri: Uri, name: String): Uri? {
    return try {
        val parentId = runCatching { DocumentsContract.getDocumentId(folderUri) }.getOrNull()
            ?: DocumentsContract.getTreeDocumentId(folderUri)
        val parent = DocumentsContract.buildDocumentUriUsingTree(folderUri, parentId)
        DocumentsContract.createDocument(contentResolver, parent, DocumentsContract.Document.MIME_TYPE_DIR, name)
    } catch (_: FileNotFoundException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    } catch (_: UnsupportedOperationException) {
        null
    }
}
