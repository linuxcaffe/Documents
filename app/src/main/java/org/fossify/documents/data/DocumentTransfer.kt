package org.fossify.documents.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.net.toUri
import java.io.FileNotFoundException
import java.io.IOException

internal enum class TransferMode { COPY, MOVE }

/** How a move or copy went: the files done (old URI to new), the names skipped, the count that failed. */
internal data class TransferResult(
    val done: Map<String, String> = emptyMap(),
    val skipped: List<String> = emptyList(),
    val failed: Int = 0,
)

/**
 * Copies (or moves) files into [targetFolderUri] by streaming their contents: Termux's provider, for one,
 * can neither copy nor move. A move deletes the original only once the copy is complete. A name the target
 * already has is skipped, never overwritten or renamed.
 */
internal fun Context.transferDocuments(
    files: List<Pair<String, String>>,
    targetFolderUri: Uri,
    mode: TransferMode,
): TransferResult {
    val target = targetDocumentUri(targetFolderUri) ?: return TransferResult(failed = files.size)
    val taken = childNames(target).toMutableSet()
    val done = mutableMapOf<String, String>()
    val skipped = mutableListOf<String>()
    var failed = 0
    for ((uri, name) in files) {
        if (name in taken) {
            skipped += name
            continue
        }
        val copy = copyInto(uri.toUri(), target, name)
        when {
            copy == null -> failed++
            mode == TransferMode.MOVE && !deleteQuietly(uri.toUri()) -> {
                deleteQuietly(copy)   // the original stays: don't leave two
                failed++
            }

            else -> {
                done[uri] = copy.toString()
                taken += name
            }
        }
    }
    return TransferResult(done, skipped, failed)
}

private fun Context.copyInto(source: Uri, target: Uri, name: String): Uri? {
    val mimeType = contentResolver.getType(source) ?: "application/octet-stream"
    val created = try {
        DocumentsContract.createDocument(contentResolver, target, mimeType, name)
    } catch (_: FileNotFoundException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    } ?: return null
    // a provider may change the name (a suffix when it's taken, an extension for the type)
    if (displayName(created) == name && copyContents(source, created)) return created
    deleteQuietly(created)
    return null
}

private fun Context.copyContents(from: Uri, to: Uri): Boolean {
    return try {
        contentResolver.openInputStream(from)?.use { input ->
            contentResolver.openOutputStream(to, "wt")?.use { output -> input.copyTo(output) }
        } != null
    } catch (_: IOException) {
        false
    } catch (_: SecurityException) {
        false
    }
}

private fun Context.deleteQuietly(uri: Uri): Boolean {
    return try {
        DocumentsContract.deleteDocument(contentResolver, uri)
    } catch (_: FileNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    } catch (_: UnsupportedOperationException) {
        false
    }
}

private fun targetDocumentUri(folderUri: Uri): Uri? {
    val id = runCatching { DocumentsContract.getDocumentId(folderUri) }.getOrNull()
        ?: runCatching { DocumentsContract.getTreeDocumentId(folderUri) }.getOrNull()
        ?: return null
    return DocumentsContract.buildDocumentUriUsingTree(folderUri, id)
}

private fun Context.childNames(parent: Uri): Set<String> {
    val children = DocumentsContract.buildChildDocumentsUriUsingTree(parent, DocumentsContract.getDocumentId(parent))
    val projection = arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
    return try {
        contentResolver.query(children, projection, null, null, null)?.use { cursor ->
            buildSet { while (cursor.moveToNext()) cursor.getString(0)?.let(::add) }
        }.orEmpty()
    } catch (_: SecurityException) {
        emptySet()
    } catch (_: IllegalArgumentException) {
        emptySet()
    }
}

private fun Context.displayName(uri: Uri): String? {
    val projection = arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
    return try {
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}
