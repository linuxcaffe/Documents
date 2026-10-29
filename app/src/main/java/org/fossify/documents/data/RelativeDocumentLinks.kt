package org.fossify.documents.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract

/**
 * Finds the file a relative link points to. This only works for documents opened from a folder:
 * their URI carries the folder grant, which also covers the files next to them.
 */
internal fun Context.resolveRelativeDocumentLink(currentUri: Uri, destination: String): Uri? {
    val currentDocumentId = currentUri.takeIf { DocumentsContract.isTreeUri(it) }
        ?.let { runCatching { DocumentsContract.getDocumentId(it) }.getOrNull() }

    return currentDocumentId
        ?.let { resolveRelativeDocumentId(it, destination) }
        ?.let { DocumentsContract.buildDocumentUriUsingTree(currentUri, it) }
        ?.takeIf { DocumentUriPermissions(this).isReadable(it) }
}
