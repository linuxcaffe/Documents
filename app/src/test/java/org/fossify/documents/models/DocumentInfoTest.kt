package org.fossify.documents.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DocumentInfoTest {
    @Test
    fun `turns path-style document IDs into paths`() {
        val termux = "/data/data/com.termux/files/home/.nb/mobile/plan.md"
        assertEquals(termux, DocumentInfo.pathFromDocumentId(termux, "/storage/emulated/0"))
        assertEquals(
            "/storage/emulated/0/Notes/a.md",
            DocumentInfo.pathFromDocumentId("primary:Notes/a.md", "/storage/emulated/0/"),
        )
        assertEquals("/storage/emulated/0", DocumentInfo.pathFromDocumentId("primary:", "/storage/emulated/0"))
        assertNull(DocumentInfo.pathFromDocumentId("msf:1234", "/storage/emulated/0"))
    }

    @Test
    fun `makes a markdown link to a sibling file`() {
        assertEquals("[plan.md](plan.md)", DocumentInfo.markdownLink("plan.md"))
        assertEquals("[buy milk.md](buy%20milk.md)", DocumentInfo.markdownLink("buy milk.md"))
        assertEquals("[a (2).md](a%20%282%29.md)", DocumentInfo.markdownLink("a (2).md"))
    }
}
