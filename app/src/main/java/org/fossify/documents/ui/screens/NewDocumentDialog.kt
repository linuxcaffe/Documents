package org.fossify.documents.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import org.fossify.documents.R
import org.fossify.documents.data.isValidNewDocumentName
import org.fossify.documents.models.NewDocumentType

@Composable
internal fun NewDocumentDialog(
    type: NewDocumentType,
    folderName: String,
    onCreate: (String) -> Unit,
    onChooseLocation: () -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    val isValid = isValidNewDocumentName(name)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    id = when (type) {
                        NewDocumentType.TEXT -> R.string.new_text_file
                        NewDocumentType.MARKDOWN -> R.string.new_markdown_file
                    }
                )
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(text = stringResource(id = R.string.new_file_name)) },
                placeholder = { Text(text = ".${type.extension}") },
                supportingText = {
                    Text(text = stringResource(id = R.string.new_file_name_hint, folderName))
                },
                isError = !isValid,
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onCreate(name) }, enabled = isValid) {
                Text(text = stringResource(id = R.string.create_document))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onChooseLocation) {
                    Text(text = stringResource(id = R.string.choose_location))
                }
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = org.fossify.commons.R.string.cancel))
                }
            }
        },
    )
}
