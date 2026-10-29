package org.fossify.documents.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.commonmark.ext.front.matter.YamlFrontMatterBlock
import org.commonmark.ext.front.matter.YamlFrontMatterNode
import org.fossify.commons.compose.theme.SimpleTheme

@Composable
internal fun MarkdownFrontMatter(
    node: YamlFrontMatterBlock,
    textZoom: Float,
) {
    val entries = node.frontMatterEntries()
    if (entries.isEmpty()) return

    Surface(
        color = markdownNeutralSurfaceColor(CONTAINER_TINT_ALPHA),
        contentColor = SimpleTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            entries.forEach { (key, value) ->
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                            append(key)
                        }
                        append("  ")
                        append(value)
                    },
                    style = SimpleTheme.typography.bodySmall.documentTextZoomed(textZoom),
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
    }
}

internal fun YamlFrontMatterBlock.frontMatterEntries(): List<Pair<String, String>> {
    return children()
        .filterIsInstance<YamlFrontMatterNode>()
        .map { it.key to it.values.joinToString(", ") }
}
