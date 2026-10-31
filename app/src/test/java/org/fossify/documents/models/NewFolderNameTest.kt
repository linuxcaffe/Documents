package org.fossify.documents.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewFolderNameTest {
    @Test
    fun `accepts a plain name, trimmed`() {
        assertEquals("drafts", NewFolderName.target(" drafts "))
        assertEquals("2026 trips", NewFolderName.target("2026 trips"))
    }

    @Test
    fun `refuses names that aren't one folder name`() {
        assertNull(NewFolderName.target(""))
        assertNull(NewFolderName.target("  "))
        assertNull(NewFolderName.target("a/b"))
        assertNull(NewFolderName.target("."))
        assertNull(NewFolderName.target(".."))
    }
}
