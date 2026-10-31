package org.fossify.documents.activities

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.fossify.commons.extensions.toast
import org.fossify.documents.R
import org.fossify.documents.data.DocumentsRepository
import org.fossify.documents.data.canDeleteDocument

/** Whether the viewed file may be deleted: asked once, off the main thread; false until known. */
@Composable
internal fun rememberCanDeleteDocument(uri: Uri?): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = false, uri) {
        value = uri != null && withContext(Dispatchers.IO) { context.canDeleteDocument(uri) }
    }
}

/** A viewer's Delete, after the user confirmed: the file goes, then the viewer closes. */
internal fun ComponentActivity.deleteViewedDocument(uri: Uri) {
    lifecycleScope.launch {
        val deleted = withContext(Dispatchers.IO) {
            DocumentsRepository(application).deleteDocuments(listOf(uri.toString())).isNotEmpty()
        }
        if (deleted) {
            toast(resources.getQuantityString(R.plurals.delete_files_done, 1, 1))
            finish()
        } else {
            toast(resources.getQuantityString(R.plurals.delete_files_failed, 1, 1))
        }
    }
}
