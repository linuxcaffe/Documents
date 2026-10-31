package org.fossify.documents.models

/** The rules for a new file name, kept apart from Android so they can be tested. */
object DocumentRename {
    /** The name to rename to, or null when it can't be used (blank, a path, `.` or `..`). */
    fun target(input: String): String? {
        val name = input.trim()
        return name.takeUnless { it.isEmpty() || '/' in it || it == "." || it == ".." }
    }

    /** How much of the name the dialog selects at first: the part before the extension. */
    fun stemLength(name: String): Int {
        val dot = name.lastIndexOf('.')
        return if (dot > 0) dot else name.length
    }
}
