package dev.anaes.qrh.search

import dev.anaes.qrh.model.Guideline

data class SearchResult(
    val guideline: Guideline,
    val snippet: String? = null,
    val matchStart: Int = -1,
    val matchEnd: Int = -1,
    val titleMatchStart: Int = -1,
    val titleMatchEnd: Int = -1,
)

/**
 * Case-insensitive search over guideline codes, titles and content.
 *
 * Plain Kotlin with no Android dependencies: the HTML stripper is injected, so the app can use
 * `android.text.Html` while JVM unit tests pass a simple regex. Indexes are built lazily on first
 * search, so call [search] off the main thread.
 */
class GuidelineSearch(
    private val guidelines: List<Guideline>,
    private val stripHtml: (String) -> String,
) {

    // Plain text and its lowercased form for each guideline's content, keyed by code
    private val textIndex: Map<String, Pair<String, String>> by lazy {
        guidelines.associate { guideline ->
            val text = guideline.content.joinToString(" ") { item ->
                val headText = if (item.head.isNotBlank()) stripHtml(item.head) else ""
                val bodyText = if (item.body.isNotBlank()) stripHtml(item.body) else ""
                "$headText $bodyText"
            }.replace(WHITESPACE_REGEX, " ").trim()
            guideline.code to (text to text.lowercase())
        }
    }

    // Lowercased "code title" strings, keyed by code
    private val titleIndex: Map<String, String> by lazy {
        guidelines.associate { it.code to "${it.code} ${it.title}".lowercase() }
    }

    /**
     * Returns guidelines matching [query] in list order. A code/title match takes precedence and
     * carries the title highlight range; otherwise a content match carries a snippet around the
     * first occurrence.
     */
    fun search(query: String): List<SearchResult> {
        val lowerQuery = query.trim().lowercase()
        if (lowerQuery.isEmpty()) return emptyList()
        val results = mutableListOf<SearchResult>()

        for (guideline in guidelines) {
            val titleMatch = titleIndex[guideline.code] ?: ""
            if (titleMatch.contains(lowerQuery)) {
                // Find where the match is within the title itself (not the "code title" combo)
                val titleIdx = guideline.title.lowercase().indexOf(lowerQuery)
                results.add(
                    SearchResult(
                        guideline = guideline,
                        titleMatchStart = titleIdx,
                        titleMatchEnd = if (titleIdx >= 0) titleIdx + lowerQuery.length else -1,
                    )
                )
                continue
            }

            val (fullText, lowerText) = textIndex[guideline.code] ?: continue
            val matchIndex = lowerText.indexOf(lowerQuery)
            if (matchIndex >= 0) {
                val snippet = extractSnippet(fullText, matchIndex, lowerQuery.length)
                results.add(
                    SearchResult(
                        guideline = guideline,
                        snippet = snippet.text,
                        matchStart = snippet.highlightStart,
                        matchEnd = snippet.highlightEnd,
                    )
                )
            }
        }

        return results
    }

    internal data class Snippet(
        val text: String,
        val highlightStart: Int,
        val highlightEnd: Int,
    )

    internal companion object {
        private const val SNIPPET_CONTEXT_CHARS = 40
        private val WHITESPACE_REGEX = "\\s+".toRegex()

        /**
         * Cuts a snippet of roughly [SNIPPET_CONTEXT_CHARS] either side of the match, trimmed to
         * word boundaries and ellipsised where text was cut.
         */
        fun extractSnippet(text: String, matchIndex: Int, matchLength: Int): Snippet {
            val snippetStart = (matchIndex - SNIPPET_CONTEXT_CHARS).coerceAtLeast(0)
            val snippetEnd = (matchIndex + matchLength + SNIPPET_CONTEXT_CHARS).coerceAtMost(text.length)

            val adjustedStart = if (snippetStart > 0) {
                val spaceIndex = text.indexOf(' ', snippetStart)
                if (spaceIndex in snippetStart until matchIndex) spaceIndex + 1 else snippetStart
            } else snippetStart

            val adjustedEnd = if (snippetEnd < text.length) {
                val spaceIndex = text.lastIndexOf(' ', snippetEnd)
                if (spaceIndex > matchIndex + matchLength) spaceIndex else snippetEnd
            } else snippetEnd

            val prefix = if (adjustedStart > 0) "…" else ""
            val suffix = if (adjustedEnd < text.length) "…" else ""

            val snippetText = prefix + text.substring(adjustedStart, adjustedEnd) + suffix
            val highlightStart = prefix.length + (matchIndex - adjustedStart)
            val highlightEnd = highlightStart + matchLength

            return Snippet(snippetText, highlightStart, highlightEnd)
        }
    }
}
