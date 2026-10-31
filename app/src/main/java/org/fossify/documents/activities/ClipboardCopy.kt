package org.fossify.documents.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import org.fossify.commons.extensions.toast
import org.fossify.commons.helpers.isTiramisuPlus
import org.fossify.documents.R

/** Puts text on the clipboard; Android 13+ confirms that itself, older versions get a toast. */
internal fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.info_title), text))
    if (!isTiramisuPlus()) {
        toast(R.string.info_copied)
    }
}
