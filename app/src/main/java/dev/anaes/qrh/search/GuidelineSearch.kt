package dev.anaes.qrh.search

import dev.anaes.qrh.model.ContentType
import dev.anaes.qrh.model.Guideline

/**
 * A guideline matching a search. Highlight ranges are `start until end` ranges of the title and
 * snippet; [cardIndex] is the content item the snippet came from (-1 unless matched by content).
 */
data class SearchResult(
    val guideline: Guideline,
    val terms: List<SearchTerm> = emptyList(),
    val titleHighlights: List<IntRange> = emptyList(),
    val snippet: String? = null,
    val snippetHighlights: List<IntRange> = emptyList(),
    val cardIndex: Int = -1,
) {
    val isContentMatch: Boolean get() = cardIndex >= 0
}

/**
 * Searches guideline codes, titles and content, as qrh-ios does. Results come in three groups,
 * each in list order: code matches ("3-8", or "3-" for every 3-x guideline), title matches, then
 * guidelines whose text contains every word of the query, with a snippet from the item that best
 * matches. Matching ignores case, accents and punctuation (see [SearchText]). Content is only
 * searched once the query has [MINIMUM_CONTENT_LENGTH] letters or digits.
 *
 * Plain Kotlin with no Android dependencies: the HTML stripper is injected, so the app can use
 * `android.text.Html` while JVM unit tests pass a simple regex. Indexes are built lazily on first
 * search, so call [search] off the main thread.
 */
class GuidelineSearch(
    private val guidelines: List<Guideline>,
    private val stripHtml: (String) -> String,
) {

    private class Card(val index: Int, val text: SearchText)

    private class Entry(val guideline: Guideline, val title: SearchText, val cards: List<Card>)

    private val entries: List<Entry> by lazy {
        guidelines.map { guideline ->
            Entry(
                guideline = guideline,
                title = SearchText(guideline.title),
                cards = guideline.content.mapIndexedNotNull { index, item ->
                    // Banners (START) are boilerplate; an image's body is its file name. Branch
                    // conditions ("If circulatory arrest") are searched.
                    if (item.type == ContentType.BANNER) return@mapIndexedNotNull null
                    val body = if (item.type == ContentType.IMAGE) "" else item.body
                    val text = listOf(item.head, body)
                        .filter { it.isNotBlank() }
                        .joinToString(" ") { stripHtml(it) }
                        .replace(WHITESPACE_REGEX, " ")
                        .trim()
                    if (text.isEmpty()) null else Card(index, SearchText(text))
                },
            )
        }
    }

    fun search(query: String): List<SearchResult> {
        codePrefix(query)?.let { prefix ->
            val matches = guidelines.filter { it.code.startsWith(prefix) }
            return (matches.filter { it.code == prefix } + matches.filter { it.code != prefix })
                .map { SearchResult(guideline = it) }
        }

        val terms = SearchTerm.terms(query)
        if (terms.isEmpty()) return emptyList()
        val searchContent = terms.sumOf { it.folded.length } >= MINIMUM_CONTENT_LENGTH

        val titles = mutableListOf<SearchResult>()
        val contents = mutableListOf<SearchResult>()
        for (entry in entries) {
            if (entry.title.containsAll(terms)) {
                titles.add(SearchResult(entry.guideline, terms, highlightRanges(entry.guideline.title, terms)))
            } else if (searchContent) {
                contentMatch(entry, terms)?.let(contents::add)
            }
        }
        return titles + contents
    }

    /**
     * Every word must appear somewhere in the guideline (title or content). The snippet comes from
     * the first item holding the most of them.
     */
    private fun contentMatch(entry: Entry, terms: List<SearchTerm>): SearchResult? {
        val found = terms.all { term -> entry.title.contains(term) || entry.cards.any { it.text.contains(term) } }
        if (!found) return null

        var best: Card? = null
        var bestCount = 0
        for (card in entry.cards) {
            val count = terms.count(card.text::contains)
            if (count > bestCount) {
                best = card
                bestCount = count
            }
        }
        val card = best ?: return null
        val range = terms.firstNotNullOfOrNull(card.text::firstRange) ?: return null
        val snippet = extractSnippet(card.text.original, range.first, range.last + 1 - range.first).text
        return SearchResult(
            guideline = entry.guideline,
            terms = terms,
            titleHighlights = highlightRanges(entry.guideline.title, terms),
            snippet = snippet,
            snippetHighlights = highlightRanges(snippet, terms),
            cardIndex = card.index,
        )
    }

    internal data class Snippet(
        val text: String,
        val highlightStart: Int,
        val highlightEnd: Int,
    )

    internal companion object {
        private const val SNIPPET_CONTEXT_CHARS = 40

        /** Queries shorter than this (once folded) only search codes and titles. */
        const val MINIMUM_CONTENT_LENGTH = 3

        private val WHITESPACE_REGEX = "\\s+".toRegex()
        private val CODE_REGEX = "[0-9]-[0-9]{0,2}".toRegex()

        /** The code prefix a query asks for: "3-8", "3-", "→3-8" or "(3-8)"; otherwise null. */
        fun codePrefix(query: String): String? {
            val trimmed = query.filter { !it.isWhitespace() && it !in "()→" }
            return trimmed.takeIf { CODE_REGEX.matches(it) }
        }

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
