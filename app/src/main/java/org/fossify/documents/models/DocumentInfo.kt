package org.fossify.documents.models

/** What the Info dialog shows for one file or folder. Null fields are unknown and left out. */
data class DocumentInfo(
    val name: String,
    val isFolder: Boolean,
    val mimeType: String? = null,
    val size: Long? = null,
    val itemCount: Int? = null,
    val lastModified: Long? = null,
    val location: String? = null,
    val storage: String? = null,
    val path: String? = null,
    val canWrite: Boolean = false,
    val canDelete: Boolean = false,
    val canRename: Boolean = false,
    val canCreate: Boolean = false,
    val lastOpened: Long? = null,
    val isFavorite: Boolean = false,
) {
    companion object {
        /**
         * The file system path a document ID stands for, when it says: Termux's IDs are absolute paths,
         * external storage's are `primary:Notes/a.md` ([primaryRoot] is that volume's directory).
         */
        fun pathFromDocumentId(documentId: String, primaryRoot: String): String? = when {
            documentId.startsWith("/") -> documentId
            documentId.startsWith("primary:") ->
                listOf(primaryRoot.trimEnd('/'), documentId.removePrefix("primary:")).filter { it.isNotEmpty() }
                    .joinToString("/")

            else -> null
        }

        /** A Markdown link to a file in the same folder: `[a b.md](a%20b.md)`. */
        fun markdownLink(name: String): String {
            val target = buildString {
                for (char in name) {
                    when (char) {
                        ' ' -> append("%20")
                        '(' -> append("%28")
                        ')' -> append("%29")
                        else -> append(char)
                    }
                }
            }
            return "[$name]($target)"
        }
    }
}
