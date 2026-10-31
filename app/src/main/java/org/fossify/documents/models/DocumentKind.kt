package org.fossify.documents.models

import android.net.Uri

enum class DocumentKind {
    PDF,
    DOCX,
    TEXT,
    MARKDOWN,
    CSV,
    HTML,
    OTHER;

    companion object {
        fun from(uri: Uri, mimeType: String): DocumentKind {
            return fromName(uri.lastPathSegment.orEmpty().substringAfterLast('/'), mimeType)
        }

        fun fromName(fileName: String, mimeType: String): DocumentKind {
            val name = fileName.lowercase()
            // A leading dot marks a hidden file (.taskrc), not an extension.
            val extension = if (name.lastIndexOf('.') > 0) name.substringAfterLast('.') else ""
            val normalizedMimeType = mimeType.substringBefore(';').lowercase()
            return mimeKinds[normalizedMimeType]
                ?: extensionKinds[extension]
                ?: TEXT.takeIf {
                    normalizedMimeType.startsWith("text/") || normalizedMimeType in textApplicationMimeTypes
                }
                ?: TEXT.takeIf { isPossiblyText(extension, normalizedMimeType) }
                ?: OTHER
        }

        /**
         * Files the provider can't type (vitrc, .taskrc, foo.conf.local) are often text, so they are
         * offered as text; [org.fossify.documents.data.TextDocumentCodec] rejects them on opening
         * if they turn out to be binary.
         */
        private fun isPossiblyText(extension: String, mimeType: String): Boolean {
            return when (mimeType) {
                GENERIC_BINARY_MIME_TYPE -> extension !in binaryExtensions
                "" -> extension.isEmpty()
                else -> false
            }
        }

        private const val GENERIC_BINARY_MIME_TYPE = "application/octet-stream"
        private val binaryExtensions = setOf(
            "bin", "dat", "db", "sqlite", "so", "o", "a", "class", "dex", "img", "iso", "xz", "zst", "7z",
        )

        private val textExtensions = setOf(
            "txt",
            "log",
            "json",
            "xml",
            "yaml",
            "yml",
            "toml",
            "ini",
            "conf",
            "cfg",
            "properties",
            "ics",
            "vcf",
            "srt",
            "org",
            "typ",
            "tex",
            "adoc",
            "asciidoc",
            "rst",
            "rc",
            "env",
            "sh",
            "bash",
            "zsh",
            "fish",
            "py",
            "js",
            "ts",
            "rs",
            "c",
            "h",
            "cpp",
            "kt",
            "kts",
            "java",
            "go",
            "rb",
            "lua",
            "pl",
            "journal",
            "ledger",
            "hledger",
            "timedot",
        )
        private val extensionKinds = mapOf(
            "pdf" to PDF,
            "docx" to DOCX,
            "md" to MARKDOWN,
            "markdown" to MARKDOWN,
            "mdown" to MARKDOWN,
            "mkd" to MARKDOWN,
            "csv" to CSV,
            "tsv" to CSV,
            "html" to HTML,
            "htm" to HTML,
            "xhtml" to HTML,
        ) + textExtensions.associateWith { TEXT }
        private val mimeKinds = mapOf(
            "application/pdf" to PDF,
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to DOCX,
            "text/markdown" to MARKDOWN,
            "text/x-markdown" to MARKDOWN,
            "text/csv" to CSV,
            "text/comma-separated-values" to CSV,
            "text/tab-separated-values" to CSV,
            "application/csv" to CSV,
            "text/html" to HTML,
            "application/xhtml+xml" to HTML,
        )
        private val textApplicationMimeTypes = setOf(
            "application/x-tex",
            "application/json",
            "application/xml",
            "application/yaml",
            "application/x-yaml",
            "application/toml",
        )
    }
}
