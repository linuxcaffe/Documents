package org.fossify.documents.ui.screens

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.fossify.commons.compose.theme.SimpleTheme
import org.fossify.documents.R
import org.fossify.documents.models.DocumentKind
import org.fossify.documents.models.DocumentRename

/** Asks for a file's new name, with the part before the extension selected. */
@Composable
internal fun RenameDocumentDialog(
    name: String,
    kind: DocumentKind,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var input by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(name, TextRange(0, DocumentRename.stemLength(name))))
    }
    val target = DocumentRename.target(input.text)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(org.fossify.commons.R.string.rename)) },
        text = {
            Column {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.focusRequester(focusRequester),
                    singleLine = true,
                    isError = target == null,
                    supportingText = if (target == null) {
                        { Text(stringResource(R.string.rename_invalid_name)) }
                    } else {
                        null
                    },
                )
                if (kind == DocumentKind.MARKDOWN) {
                    Text(
                        text = stringResource(R.string.rename_links_hint),
                        color = SimpleTheme.colorScheme.onSurfaceVariant,
                        style = SimpleTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { target?.let(onRename) },
                enabled = target != null && target != name,
            ) {
                Text(stringResource(org.fossify.commons.R.string.rename))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(org.fossify.commons.R.string.cancel))
            }
        },
    )
}
