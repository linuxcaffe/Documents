package org.fossify.documents.models

/** The rules for a new folder's name, kept apart from Android so they can be tested. */
object NewFolderName {
    /** The name to create, or null when it can't be used (blank, a path, `.` or `..`). */
    fun target(input: String): String? {
        val name = input.trim()
        return name.takeUnless { it.isEmpty() || '/' in it || it == "." || it == ".." }
    }
}
