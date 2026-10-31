package org.fossify.documents.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.fossify.commons.compose.theme.SimpleTheme
import org.fossify.documents.R
import org.fossify.documents.models.DocumentFolder

/**
 * Picks the folder to move or copy into: the folders opened in the app, then their subfolders
 * ([loadChildren]), or Android's own picker for anywhere else ([onOtherFolder]).
 */
@Composable
internal fun FolderChooserDialog(
    title: String,
    confirmLabel: String,
    roots: List<DocumentFolder>,
    loadChildren: suspend (DocumentFolder) -> List<DocumentFolder>,
    onChoose: (DocumentFolder) -> Unit,
    onOtherFolder: () -> Unit,
    onDismiss: () -> Unit,
) {
    var path by remember { mutableStateOf(emptyList<DocumentFolder>()) }
    val current = path.lastOrNull()
    val folders by produceState<List<DocumentFolder>?>(initialValue = null, current) {
        value = if (current == null) roots else loadChildren(current)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(current?.name ?: title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column {
                if (current != null) {
                    FolderChooserRow(
                        icon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) },
                        label = path.getOrNull(path.size - 2)?.name ?: title,
                        onClick = { path = path.dropLast(1) },
                    )
                }
                val shown = folders
                when {
                    shown == null -> CircularProgressIndicator(
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.CenterHorizontally),
                    )

                    shown.isEmpty() -> Text(
                        text = stringResource(R.string.transfer_no_folders),
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = SimpleTheme.colorScheme.onSurfaceVariant,
                    )

                    else -> LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(shown, key = { it.uri }) { folder ->
                            FolderChooserRow(
                                icon = { Icon(Icons.Filled.Folder, contentDescription = null) },
                                label = folder.name,
                                onClick = { path = path + folder },
                            )
                        }
                    }
                }
                TextButton(onClick = onOtherFolder) {
                    Text(stringResource(R.string.transfer_other_folder))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { current?.let(onChoose) }, enabled = current != null) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(org.fossify.commons.R.string.cancel))
            }
        },
    )
}

@Composable
private fun FolderChooserRow(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        icon()
        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
