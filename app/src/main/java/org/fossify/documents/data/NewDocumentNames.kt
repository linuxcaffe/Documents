package org.fossify.documents.data

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val TIMESTAMP_NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

/**
 * Turns what the user typed into a file name: a blank name becomes a timestamp (the same
 * scheme nb uses for untitled notes), and a name without an extension gets [extension].
 */
internal fun newDocumentFileName(
    input: String,
    extension: String,
    now: LocalDateTime = LocalDateTime.now(),
): String {
    val name = input.trim().trimEnd('.').ifEmpty { now.format(TIMESTAMP_NAME_FORMAT) }
    val hasExtension = name.lastIndexOf('.') > 0
    return if (hasExtension) name else "$name.$extension"
}

internal fun isValidNewDocumentName(input: String): Boolean {
    val name = input.trim()
    return '/' !in name && name != "." && name != ".."
}
