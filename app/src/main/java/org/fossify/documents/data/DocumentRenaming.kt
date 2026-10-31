package org.fossify.documents.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.FileNotFoundException
import java.io.IOException

internal sealed interface RenameOutcome {
    data class Renamed(val uri: Uri) : RenameOutcome
    data object NameTaken : RenameOutcome
    data object Failed : RenameOutcome
}

/**
 * Renames the file in its folder. Some providers (Termux's among them) can't rename; then, when the
 * file's folder is known, it's copied to a new file with the new name and the original deleted.
 */
internal fun Context.renameDocumentFile(uri: Uri, name: String, folderUri: Uri?): RenameOutcome {
    renameInPlace(uri, name)?.let { return RenameOutcome.Renamed(it) }
    val parent = folderUri?.let(::folderDocumentUri) ?: return RenameOutcome.Failed
    if (childNames(parent).contains(name)) return RenameOutcome.NameTaken
    return copyThenDelete(uri, parent, name)
}

private fun Context.renameInPlace(uri: Uri, name: String): Uri? {
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

private fun Context.copyThenDelete(uri: Uri, parent: Uri, name: String): RenameOutcome {
    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
    val created = try {
        DocumentsContract.createDocument(contentResolver, parent, mimeType, name)
    } catch (_: FileNotFoundException) {
        null
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    } ?: return RenameOutcome.Failed
    // a provider may change the name (a suffix when it's taken, an extension for the type)
    val copied = displayName(created) == name && copyContents(uri, created)
    if (copied && deleteQuietly(uri)) return RenameOutcome.Renamed(created)
    deleteQuietly(created)
    return RenameOutcome.Failed
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

private fun folderDocumentUri(folderUri: Uri): Uri? {
    val id = runCatching { DocumentsContract.getDocumentId(folderUri) }.getOrNull()
        ?: runCatching { DocumentsContract.getTreeDocumentId(folderUri) }.getOrNull()
        ?: return null
    return DocumentsContract.buildDocumentUriUsingTree(folderUri, id)
}

private fun Context.childNames(parent: Uri): Set<String> {
    val parentId = DocumentsContract.getDocumentId(parent)
    val children = DocumentsContract.buildChildDocumentsUriUsingTree(parent, parentId)
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
