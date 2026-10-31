package org.fossify.documents.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DocumentRenameTest {
    @Test
    fun `accepts a plain name, trimmed`() {
        assertEquals("plan.md", DocumentRename.target("  plan.md "))
        assertEquals("notes", DocumentRename.target("notes"))
        assertEquals(".bashrc", DocumentRename.target(".bashrc"))
    }

    @Test
    fun `refuses names that aren't one file name`() {
        assertNull(DocumentRename.target(""))
        assertNull(DocumentRename.target("   "))
        assertNull(DocumentRename.target("work/plan.md"))
        assertNull(DocumentRename.target("."))
        assertNull(DocumentRename.target(".."))
    }

    @Test
    fun `selects the name without its extension`() {
        assertEquals(4, DocumentRename.stemLength("plan.md"))
        assertEquals(11, DocumentRename.stemLength("archive.tar.gz"))
        assertEquals(7, DocumentRename.stemLength(".bashrc"))
        assertEquals(5, DocumentRename.stemLength("notes"))
    }
}
