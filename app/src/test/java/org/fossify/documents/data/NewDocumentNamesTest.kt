package org.fossify.documents.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class NewDocumentNamesTest {
    private val now = LocalDateTime.of(2026, 10, 29, 14, 5, 9)

    @Test
    fun `blank names become timestamps`() {
        assertEquals("20261029140509.md", newDocumentFileName("", "md", now))
        assertEquals("20261029140509.txt", newDocumentFileName("   ", "txt", now))
    }

    @Test
    fun `adds the extension only when missing`() {
        assertEquals("Buy milk.md", newDocumentFileName(" Buy milk ", "md", now))
        assertEquals("notes.txt", newDocumentFileName("notes.txt", "md", now))
        assertEquals("notes.md", newDocumentFileName("notes.", "md", now))
        assertEquals(".hidden.md", newDocumentFileName(".hidden", "md", now))
    }

    @Test
    fun `rejects names that are not plain file names`() {
        assertTrue(isValidNewDocumentName(""))
        assertTrue(isValidNewDocumentName("Buy milk"))
        assertFalse(isValidNewDocumentName("a/b"))
        assertFalse(isValidNewDocumentName(".."))
    }
}
