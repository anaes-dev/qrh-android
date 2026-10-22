package dev.anaes.qrh.search

import java.text.Normalizer

/**
 * Text folded for forgiving matching, as in qrh-ios: case, accents and punctuation are ignored
 * ("Hyper-kalaemia" matches "hyperkalaemia"), and subscripts and superscripts become plain digits
 * ("SpO₂" matches "spo2"). Each folded character remembers where it came from, so matches map
 * back to ranges of the original text.
 *
 * Short words (see [SearchTerm.isShort]) only match at the start of a word, so "a" finds
 * "Anaphylaxis" without flagging every "a" in the handbook.
 */
class SearchText(val original: String) {

    /** The folded text: letters and digits, with single spaces between words. */
    internal val folded: String

    // Original [start, end) of each folded character
    private val starts: IntArray
    private val ends: IntArray

    init {
        val folded = StringBuilder()
        val starts = ArrayList<Int>()
        val ends = ArrayList<Int>()
        var lastWasSpace = true
        var i = 0
        while (i < original.length) {
            val next = i + Character.charCount(original.codePointAt(i))
            for (c in fold(original.substring(i, next))) {
                if (c.isLetterOrDigit()) {
                    folded.append(c)
                    lastWasSpace = false
                } else if (c.isWhitespace() && !lastWasSpace) {
                    // Words stay apart; other punctuation is dropped
                    folded.append(' ')
                    lastWasSpace = true
                } else {
                    continue
                }
                starts.add(i)
                ends.add(next)
            }
            i = next
        }
        this.folded = folded.toString()
        this.starts = starts.toIntArray()
        this.ends = ends.toIntArray()
    }

    fun contains(term: SearchTerm): Boolean = matchOffset(term, 0) >= 0

    fun containsAll(terms: List<SearchTerm>): Boolean = terms.all(::contains)

    /** Ranges of [original] matching the term, in order (end exclusive). */
    fun ranges(term: SearchTerm): List<IntRange> {
        val ranges = mutableListOf<IntRange>()
        var from = 0
        while (true) {
            val offset = matchOffset(term, from)
            if (offset < 0) break
            ranges.add(originalRange(offset, term))
            from = offset + term.folded.length
        }
        return ranges
    }

    /** Range of [original] of the term's first match (end exclusive), or null. */
    fun firstRange(term: SearchTerm): IntRange? =
        matchOffset(term, 0).takeIf { it >= 0 }?.let { originalRange(it, term) }

    /** Offset in [folded] of the term's first match from [from], or -1. */
    private fun matchOffset(term: SearchTerm, from: Int): Int {
        var offset = folded.indexOf(term.folded, from)
        if (term.isShort) {
            while (offset > 0 && folded[offset - 1] != ' ') {
                offset = folded.indexOf(term.folded, offset + 1)
            }
        }
        return offset
    }

    private fun originalRange(offset: Int, term: SearchTerm): IntRange =
        starts[offset] until ends[offset + term.folded.length - 1]

    internal companion object {
        private val COMBINING_MARKS = "\\p{Mn}+".toRegex()

        fun fold(text: String): String {
            val compatible = Normalizer.normalize(text, Normalizer.Form.NFKC).lowercase()
            return Normalizer.normalize(compatible, Normalizer.Form.NFD).replace(COMBINING_MARKS, "")
        }
    }
}

/** One word of a query, folded. */
data class SearchTerm(val folded: String) {

    /** Short words ("a", "iv") only match at the start of a word. */
    val isShort: Boolean get() = folded.length < MIN_ANYWHERE_LENGTH

    companion object {
        /** Query words, folded, with punctuation-only words and repeats dropped. */
        fun terms(query: String): List<SearchTerm> =
            query.split(WHITESPACE)
                .map { SearchTerm(SearchText(it).folded.replace(" ", "")) }
                .filter { it.folded.isNotEmpty() }
                .distinct()

        private val WHITESPACE = "\\s+".toRegex()

        /** Words this long or longer also match inside words ("kalaemia" in "hyperkalaemia"). */
        const val MIN_ANYWHERE_LENGTH = 3
    }
}

/** Ranges of [text] matching any of the terms, sorted. */
fun highlightRanges(text: String, terms: List<SearchTerm>): List<IntRange> {
    if (terms.isEmpty() || text.isEmpty()) return emptyList()
    val searchText = SearchText(text)
    return terms.flatMap(searchText::ranges).sortedBy { it.first }
}
