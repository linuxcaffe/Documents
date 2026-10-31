package org.fossify.documents.activities

import android.content.Context
import org.fossify.documents.R
import org.fossify.documents.data.TransferMode
import org.fossify.documents.data.TransferResult

// names listed in the "skipped" line; more show as "…"
private const val MAX_SKIPPED_NAMES = 3

/** What a move or copy did, for one toast: done, skipped (by name) and failed; null when nothing happened. */
internal fun Context.transferReport(mode: TransferMode, result: TransferResult): String? {
    val lines = buildList {
        val done = result.done.size
        if (done > 0) {
            val plural = if (mode == TransferMode.MOVE) R.plurals.transfer_moved else R.plurals.transfer_copied
            add(resources.getQuantityString(plural, done, done))
        }
        if (result.skipped.isNotEmpty()) {
            val names = result.skipped.take(MAX_SKIPPED_NAMES).joinToString(", ") +
                    if (result.skipped.size > MAX_SKIPPED_NAMES) ", …" else ""
            add(getString(R.string.transfer_skipped, names))
        }
        if (result.failed > 0) {
            add(resources.getQuantityString(R.plurals.transfer_failed, result.failed, result.failed))
        }
    }
    return lines.joinToString("\n").takeIf { lines.isNotEmpty() }
}
