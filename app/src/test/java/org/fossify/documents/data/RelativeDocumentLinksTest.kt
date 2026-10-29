package org.fossify.documents.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RelativeDocumentLinksTest {
    private val termuxNote = "/data/data/com.termux/files/home/.nb/home/start.md"
    private val storageNote = "primary:Notes/home/start.md"

    @Test
    fun `recognises links to nearby files`() {
        assertTrue(isRelativeDocumentLink("other.md"))
        assertTrue(isRelativeDocumentLink("../work/plan.md#goals"))
        assertFalse(isRelativeDocumentLink("#section"))
        assertFalse(isRelativeDocumentLink("/etc/passwd"))
        assertFalse(isRelativeDocumentLink("https://fossify.org"))
        assertFalse(isRelativeDocumentLink("mailto:hello@fossify.org"))
        assertFalse(isRelativeDocumentLink(" "))
    }

    @Test
    fun `resolves path style document ids`() {
        assertEquals(
            "/data/data/com.termux/files/home/.nb/home/other.md",
            resolveRelativeDocumentId(termuxNote, "other.md"),
        )
        assertEquals(
            "/data/data/com.termux/files/home/.nb/work/plan.md",
            resolveRelativeDocumentId(termuxNote, "../work/./plan.md#goals"),
        )
        assertEquals("primary:Notes/home/other.md", resolveRelativeDocumentId(storageNote, "other.md"))
        assertEquals("primary:Notes/todo.md", resolveRelativeDocumentId(storageNote, "../todo.md"))
        assertEquals("primary:top.md", resolveRelativeDocumentId("primary:a.md", "top.md"))
    }

    @Test
    fun `decodes escapes but keeps plus signs`() {
        assertEquals("primary:Notes/home/Buy milk.md", resolveRelativeDocumentId(storageNote, "Buy%20milk.md"))
        assertEquals("primary:Notes/home/c++.md", resolveRelativeDocumentId(storageNote, "c++.md"))
    }

    @Test
    fun `rejects links that leave the root or point nowhere`() {
        assertNull(resolveRelativeDocumentId("primary:a.md", "../b.md"))
        assertNull(resolveRelativeDocumentId("/a.md", "../b.md"))
        assertNull(resolveRelativeDocumentId(storageNote, "#only-an-anchor"))
    }
}
