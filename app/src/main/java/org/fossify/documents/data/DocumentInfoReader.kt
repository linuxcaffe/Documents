package org.fossify.documents.data

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import org.fossify.commons.extensions.getRealPathFromURI
import org.fossify.documents.models.DocumentEntry
import org.fossify.documents.models.DocumentInfo

/** Everything the provider says about one file or folder, for the Info dialog. */
internal fun Context.readDocumentInfo(uri: Uri, fallbackName: String, entry: DocumentEntry?): DocumentInfo {
    val documentUri = documentUriOf(uri) ?: uri
    val documentId = runCatching { DocumentsContract.getDocumentId(documentUri) }.getOrNull()
    val columns = queryColumns(documentUri)
    val isFolder = columns?.mimeType == Document.MIME_TYPE_DIR
    val flags = columns?.flags ?: 0
    val resolver = DocumentLocationResolver(this)
    return DocumentInfo(
        name = columns?.name ?: fallbackName,
        isFolder = isFolder,
        mimeType = columns?.mimeType?.takeUnless { isFolder },
        size = columns?.size.takeUnless { isFolder },
        itemCount = if (isFolder) countChildren(documentUri) else null,
        lastModified = columns?.lastModified?.takeIf { it > 0 },
        location = resolver.resolveDocument(documentUri).ifBlank { null },
        storage = resolver.storageLabel(documentUri).ifBlank { null },
        path = runCatching { getRealPathFromURI(documentUri) }.getOrNull()?.ifBlank { null }
            ?: documentId?.let {
                DocumentInfo.pathFromDocumentId(it, Environment.getExternalStorageDirectory().absolutePath)
            },
        canWrite = flags and Document.FLAG_SUPPORTS_WRITE != 0,
        canDelete = flags and Document.FLAG_SUPPORTS_DELETE != 0,
        canRename = flags and Document.FLAG_SUPPORTS_RENAME != 0,
        canCreate = flags and Document.FLAG_DIR_SUPPORTS_CREATE != 0,
        lastOpened = entry?.lastOpened?.takeIf { it > 0 },
        isFavorite = entry?.isFavorite == true,
    )
}

private class InfoColumns(
    val name: String?,
    val mimeType: String?,
    val size: Long?,
    val lastModified: Long?,
    val flags: Int,
)

private fun Context.queryColumns(uri: Uri): InfoColumns? {
    val projection = arrayOf(
        Document.COLUMN_DISPLAY_NAME,
        Document.COLUMN_MIME_TYPE,
        Document.COLUMN_SIZE,
        Document.COLUMN_LAST_MODIFIED,
        Document.COLUMN_FLAGS,
    )
    return try {
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            InfoColumns(
                name = cursor.stringAt(0),
                mimeType = cursor.stringAt(1),
                size = cursor.longAt(2),
                lastModified = cursor.longAt(3),
                flags = cursor.longAt(4)?.toInt() ?: 0,
            )
        }
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}

private fun Context.countChildren(folder: Uri): Int? {
    val id = runCatching { DocumentsContract.getDocumentId(folder) }.getOrNull() ?: return null
    val children = DocumentsContract.buildChildDocumentsUriUsingTree(folder, id)
    return try {
        contentResolver.query(children, arrayOf(Document.COLUMN_DOCUMENT_ID), null, null, null)?.use { it.count }
    } catch (_: SecurityException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}

/** A folder opened from the picker is a tree URI; its own document is the tree's root. */
private fun documentUriOf(uri: Uri): Uri? {
    if (!DocumentsContract.isTreeUri(uri)) return null
    val id = runCatching { DocumentsContract.getDocumentId(uri) }.getOrNull()
        ?: runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
        ?: return null
    return DocumentsContract.buildDocumentUriUsingTree(uri, id)
}

private fun Cursor.stringAt(index: Int): String? = if (isNull(index)) null else getString(index)

private fun Cursor.longAt(index: Int): Long? = if (isNull(index)) null else getLong(index)
