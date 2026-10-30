package org.fossify.documents.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import org.fossify.documents.models.MSyncStatus
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

private const val MAX_STATUS_BYTES = 64 * 1024

/** Reads a folder's `.m-sync.json`; null when it's missing, too big or not m-sync's. */
internal fun Context.readMSyncStatus(treeUri: Uri, documentId: String): MSyncStatus? {
    val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
    return try {
        contentResolver.openInputStream(uri)?.use { it.readAtMost(MAX_STATUS_BYTES) }
            ?.let { parseMSyncStatus(it.toString(Charsets.UTF_8)) }
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    }
}

internal fun parseMSyncStatus(text: String): MSyncStatus? {
    return try {
        val json = JSONObject(text)
        val conflicts = json.optJSONArray("conflicts")
        MSyncStatus.from(
            state = json.optString("state"),
            message = json.optString("message"),
            synced = json.optString("synced").takeUnless { json.isNull("synced") },
            checked = json.optString("checked").takeUnless { json.isNull("checked") },
            waiting = json.optInt("waiting"),
            conflicts = List(conflicts?.length() ?: 0) { conflicts?.optString(it).orEmpty() },
        )
    } catch (_: JSONException) {
        null
    }
}

private fun InputStream.readAtMost(limit: Int): ByteArray? {
    val out = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val read = read(buffer)
        if (read < 0) break
        if (out.size() + read > limit) return null
        out.write(buffer, 0, read)
    }
    return out.toByteArray()
}
