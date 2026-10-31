package org.fossify.documents.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import org.fossify.commons.compose.theme.SimpleTheme
import org.fossify.documents.R

/** Asks before deleting files: there's no trash for documents opened through Android's picker. */
@Composable
internal fun DeleteDocumentsDialog(
    count: Int,
    name: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (count == 1 && name != null) {
                    stringResource(R.string.delete_file_title, name)
                } else {
                    pluralStringResource(R.plurals.delete_files_title, count, count)
                }
            )
        },
        text = { Text(stringResource(R.string.delete_files_hint)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(org.fossify.commons.R.string.delete),
                    color = SimpleTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(org.fossify.commons.R.string.cancel))
            }
        },
    )
}
