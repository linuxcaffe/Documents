package org.fossify.documents.ui.screens

import org.commonmark.ext.front.matter.YamlFrontMatterBlock
import org.commonmark.node.Heading
import org.commonmark.node.Text
import org.commonmark.node.ThematicBreak
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownFrontMatterTest {
    @Test
    fun `leading front matter becomes metadata instead of a heading`() {
        val document = markdownParser.parse(
            """
            ---
            title: Shopping list
            tags: [errands, home]
            aliases:
              - groceries
              - food
            ---
            # Body
            """.trimIndent()
        )

        val blocks = document.children()
        val frontMatter = blocks.first() as YamlFrontMatterBlock
        assertEquals(
            listOf(
                "title" to "Shopping list",
                "tags" to "[errands, home]",
                "aliases" to "groceries, food",
            ),
            frontMatter.frontMatterEntries(),
        )
        val heading = blocks.filterIsInstance<Heading>().single()
        assertEquals("Body", (heading.firstChild as Text).literal)
    }

    @Test
    fun `thematic break later in the document is not front matter`() {
        val document = markdownParser.parse("Intro\n\n---\n\nkey: value\n")

        assertTrue(document.children().none { it is YamlFrontMatterBlock })
        assertTrue(document.children().any { it is ThematicBreak })
    }
}
