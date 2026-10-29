package org.fossify.documents.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HiddenFilesTest {
    @Test
    fun `dotfiles are hidden`() {
        assertTrue(isHiddenFileName(".git"))
        assertTrue(isHiddenFileName(".index"))
        assertFalse(isHiddenFileName("notes.md"))
        assertFalse(isHiddenFileName("Buy milk.md"))
    }
}
