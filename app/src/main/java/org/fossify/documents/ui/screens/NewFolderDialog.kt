package org.fossify.documents.ui.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import org.fossify.documents.R
import org.fossify.documents.models.NewFolderName

/** Asks for the name of a folder to create inside the one being browsed. */
@Composable
internal fun NewFolderDialog(
    onCreate: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var input by rememberSaveable { mutableStateOf("") }
    val target = NewFolderName.target(input)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.documents_new_folder)) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.focusRequester(focusRequester),
                singleLine = true,
                isError = input.isNotEmpty() && target == null,
                supportingText = if (input.isNotEmpty() && target == null) {
                    { Text(stringResource(R.string.documents_new_folder_invalid_name)) }
                } else {
                    null
                },
            )
        },
        confirmButton = {
            TextButton(onClick = { target?.let(onCreate) }, enabled = target != null) {
                Text(stringResource(org.fossify.commons.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(org.fossify.commons.R.string.cancel))
            }
        },
    )
}
