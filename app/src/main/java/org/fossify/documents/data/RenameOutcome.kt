package org.fossify.documents.data

import android.net.Uri

/** What a rename came to: the file's new URI, a name already used in the folder, or a refusal. */
internal sealed interface RenameOutcome {
    data class Renamed(val uri: Uri) : RenameOutcome
    data object NameTaken : RenameOutcome
    data object Failed : RenameOutcome
}
