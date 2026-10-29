package org.fossify.documents.data

import java.net.URLDecoder

private val externalDocumentLinkSchemes = setOf("http", "https", "mailto", "tel")

internal fun isAllowedExternalDocumentLink(destination: String): Boolean {
    val scheme = destination.substringBefore(':', missingDelimiterValue = "").lowercase()
    return scheme in externalDocumentLinkSchemes
}

private val uriSchemePattern = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:")

/** A link to another file next to this one, like `other.md` or `../notes/a.md`. */
internal fun isRelativeDocumentLink(destination: String): Boolean {
    val path = destination.trim()
    return path.isNotEmpty() &&
            !path.startsWith('#') &&
            !path.startsWith('/') &&
            !uriSchemePattern.containsMatchIn(path)
}

/**
 * Resolves [destination] against the document ID of the current file. Document IDs are opaque in
 * general, but the common providers use paths (`/data/…/notes/a.md`, `primary:Notes/a.md`), and
 * those are the ones this supports. Returns null when the link leaves the provider's root.
 */
internal fun resolveRelativeDocumentId(currentDocumentId: String, destination: String): String? {
    val rootSeparator = currentDocumentId.indexOf(':')
    val hasRootPrefix = rootSeparator >= 0 && '/' !in currentDocumentId.substring(0, rootSeparator)
    val prefix = if (hasRootPrefix) currentDocumentId.substring(0, rootSeparator + 1) else ""
    val segments = currentDocumentId.removePrefix(prefix).split('/').dropLast(1).toMutableList()
    val minimumSize = if (segments.firstOrNull() == "") 1 else 0

    val linkPath = decodeLinkPath(destination.trim().substringBefore('#').substringBefore('?'))
    for (segment in linkPath.split('/')) {
        when (segment) {
            "", "." -> Unit
            ".." -> if (segments.size > minimumSize) segments.removeAt(segments.lastIndex) else return null
            else -> segments += segment
        }
    }

    val path = segments.joinToString("/")
    return if (linkPath.isBlank() || path.isEmpty()) null else prefix + path
}

private fun decodeLinkPath(path: String): String {
    // '+' is literal in paths; URLDecoder would turn it into a space.
    return runCatching { URLDecoder.decode(path.replace("+", "%2B"), Charsets.UTF_8.name()) }.getOrDefault(path)
}
