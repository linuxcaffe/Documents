package org.fossify.documents.ui.screens

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.fossify.commons.compose.theme.SimpleTheme
import org.fossify.commons.extensions.formatSize
import org.fossify.documents.R
import org.fossify.documents.models.DocumentInfo

/** Info for one selected file or folder, with its path or a Markdown link to copy. */
@Composable
internal fun DocumentInfoDialog(
    name: String,
    loadInfo: suspend () -> DocumentInfo,
    onCopy: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val info by produceState<DocumentInfo?>(initialValue = null) { value = loadInfo() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(info?.name ?: name) },
        text = {
            val shown = info
            if (shown == null) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    InfoRows(shown)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        shown.path?.let { path ->
                            TextButton(onClick = { onCopy(path) }) { Text(stringResource(R.string.info_copy_path)) }
                        }
                        if (!shown.isFolder) {
                            TextButton(onClick = { onCopy(DocumentInfo.markdownLink(shown.name)) }) {
                                Text(stringResource(R.string.info_copy_link))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(org.fossify.commons.R.string.ok)) }
        },
    )
}

@Composable
private fun InfoRows(info: DocumentInfo) {
    val context = LocalContext.current
    val dateFlags = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_YEAR
    info.mimeType?.let { InfoRow(stringResource(R.string.info_type), it) }
    info.size?.let { InfoRow(stringResource(R.string.info_size), it.formatSize()) }
    info.itemCount?.let {
        InfoRow(stringResource(R.string.info_contents), pluralStringResource(R.plurals.folder_item_count, it, it))
    }
    info.lastModified?.let {
        InfoRow(stringResource(R.string.info_modified), DateUtils.formatDateTime(context, it, dateFlags))
    }
    info.location?.let { InfoRow(stringResource(R.string.info_location), it) }
    info.storage?.let { InfoRow(stringResource(R.string.info_storage), it) }
    info.path?.let { InfoRow(stringResource(R.string.info_path), it) }
    InfoRow(stringResource(R.string.info_access), info.accessText())
    info.lastOpened?.let {
        InfoRow(stringResource(R.string.info_last_opened), DateUtils.formatDateTime(context, it, dateFlags))
    }
    if (info.isFavorite) {
        InfoRow(stringResource(R.string.info_favorite), stringResource(R.string.info_in_favorites))
    }
}

@Composable
private fun DocumentInfo.accessText(): String {
    val can = buildList {
        if (canWrite) add(stringResource(R.string.info_access_edit))
        if (canRename) add(stringResource(R.string.info_access_rename))
        if (canDelete) add(stringResource(R.string.info_access_delete))
        if (isFolder && canCreate) add(stringResource(R.string.info_access_create))
    }
    return if (can.isEmpty()) stringResource(R.string.info_access_read_only) else can.joinToString(", ")
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = SimpleTheme.typography.labelMedium,
            color = SimpleTheme.colorScheme.onSurfaceVariant,
        )
        SelectionContainer {
            Text(text = value, style = SimpleTheme.typography.bodyMedium)
        }
    }
}
