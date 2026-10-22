package dev.anaes.qrh.search

import dev.anaes.qrh.model.ContentItem
import dev.anaes.qrh.model.Guideline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidelineSearchTest {

    private val tagRegex = Regex("<[^>]*>")
    private val stripTags: (String) -> String = { it.replace(tagRegex, "").trim() }

    private fun guideline(code: String, title: String, vararg bodies: String) =
        guideline(code, title, bodies.map { ContentItem(type = 3, body = it) })

    private fun guideline(code: String, title: String, content: List<ContentItem>) = Guideline(
        title = title,
        code = code,
        version = 1,
        url = "https://example.com/$code.pdf",
        content = content,
    )

    private val guidelines = listOf(
        guideline("1-1", "Cardiac arrest", "Start <b>CPR</b> and call for help"),
        guideline("2-1", "Hypoxia", "Increase FiO2 to 1.0", "Check the <i>breathing circuit</i>"),
        guideline("3-4", "Anaphylaxis", "Give adrenaline IM; consider cardiac arrest protocol"),
        guideline(
            "4-1", "Electrolytes",
            listOf(
                ContentItem(type = 2, body = "<b>START</b>"),
                ContentItem(type = 3, body = "Check S<small>p</small>O₂ and treat Hyper-kalaemia"),
                ContentItem(type = 3, body = "Give 0.5 mg.kg⁻¹ over 2-3 min"),
                ContentItem(type = 10, head = "Box B: OXYGEN CYLINDER", body = "oxygen.png"),
            ),
        ),
    )

    private val search = GuidelineSearch(guidelines, stripTags)

    private fun SearchResult.snippetHighlightTexts() = snippetHighlights.map { snippet!!.substring(it.first, it.last + 1) }

    @Test
    fun `blank query returns no results`() {
        assertTrue(search.search("").isEmpty())
        assertTrue(search.search("   ").isEmpty())
    }

    @Test
    fun `title match is case-insensitive and carries the title highlight range`() {
        val results = search.search("HYPOX")

        assertEquals(listOf("2-1"), results.map { it.guideline.code })
        val result = results.single()
        assertEquals(listOf(0 until 5), result.titleHighlights)
        assertNull(result.snippet)
        assertFalse(result.isContentMatch)
    }

    @Test
    fun `code match is found without a title highlight`() {
        val result = search.search("3-4").single()

        assertEquals("3-4", result.guideline.code)
        assertTrue(result.titleHighlights.isEmpty())
        assertFalse(result.isContentMatch)
    }

    @Test
    fun `code prefixes list every matching guideline, exact code first`() {
        val codes = GuidelineSearch(
            listOf(guideline("3-10", "Ten"), guideline("3-1", "One"), guideline("3-2", "Two"), guideline("2-1", "Other")),
            stripTags,
        )

        assertEquals(listOf("3-1", "3-10"), codes.search("3-1").map { it.guideline.code })
        assertEquals(listOf("3-10", "3-1", "3-2"), codes.search("3-").map { it.guideline.code })
        assertEquals(listOf("3-2"), codes.search("(→ 3-2)").map { it.guideline.code })
    }

    @Test
    fun `content match returns a snippet highlighting the query and the item it came from`() {
        val result = search.search("adrenaline").single()

        assertEquals("3-4", result.guideline.code)
        assertEquals(listOf("adrenaline"), result.snippetHighlightTexts())
        assertEquals(0, result.cardIndex)
        assertTrue(result.isContentMatch)
    }

    @Test
    fun `title matches come before content matches`() {
        // "cardiac arrest" is 1-1's title and also appears in 3-4's content
        val results = search.search("cardiac arrest")

        assertEquals(listOf("1-1", "3-4"), results.map { it.guideline.code })
        assertNull(results[0].snippet)
        assertTrue(results[1].snippet != null)
    }

    @Test
    fun `HTML tags are stripped before matching content`() {
        assertTrue(search.search("<b>").isEmpty())

        val result = search.search("start cpr").single()
        assertEquals("1-1", result.guideline.code)
        assertEquals(listOf("Start", "CPR"), result.snippetHighlightTexts())
    }

    @Test
    fun `every word must appear, but not necessarily in the same item`() {
        assertEquals("2-1", search.search("breathing check").single().guideline.code)
        assertTrue(search.search("adrenaline breathing").isEmpty())

        // FiO2 and breathing are in different items: the snippet comes from the first
        val result = search.search("breathing fio2").single()
        assertEquals("2-1", result.guideline.code)
        assertEquals(0, result.cardIndex)
    }

    @Test
    fun `the snippet comes from the item holding the most words`() {
        assertEquals(1, search.search("breathing circuit").single().cardIndex)
    }

    @Test
    fun `case, accents, punctuation, subscripts and superscripts are ignored`() {
        val spo2 = search.search("spo2").single()
        assertEquals("4-1", spo2.guideline.code)
        assertEquals(1, spo2.cardIndex)
        assertEquals(listOf("SpO₂"), spo2.snippetHighlightTexts())

        assertEquals("4-1", search.search("hyperkalaemia").single().guideline.code)
        assertEquals("3-4", search.search("ANAPHYLAXÍS").single().guideline.code)

        val dose = search.search("mg.kg-1 2-3").single()
        assertEquals(2, dose.cardIndex)
        assertEquals(listOf("mg.kg⁻¹", "2-3"), dose.snippetHighlightTexts())
    }

    @Test
    fun `START banners and image file names are not searched`() {
        assertEquals(listOf("1-1"), search.search("start").map { it.guideline.code })
        assertTrue(search.search("png").isEmpty())
        assertEquals(3, search.search("oxygen cylinder").single().cardIndex)
    }

    @Test
    fun `content from every item is searched`() {
        assertEquals("2-1", search.search("breathing circuit").single().guideline.code)
    }

    @Test
    fun `query is trimmed`() {
        assertEquals("2-1", search.search("  hypoxia  ").single().guideline.code)
    }

    @Test
    fun `queries under three characters search titles only`() {
        // "ca" is in 3-4's content ("cardiac arrest protocol") but only 1-1's title
        assertEquals(listOf("1-1"), search.search("ca").map { it.guideline.code })
        assertTrue(search.search("q").isEmpty())
    }

    @Test
    fun `short words only match, and are only highlighted, at the start of a word`() {
        // Not the "a" inside "Hypoxia" or "Electrolytes"
        val results = search.search("a")
        assertEquals(listOf("1-1", "3-4"), results.map { it.guideline.code })
        assertEquals(listOf(8 until 9), results[0].titleHighlights) // Cardiac [a]rrest
        assertEquals(listOf(0 until 1), results[1].titleHighlights) // [A]naphylaxis

        val text = SearchText("Hypoxia and anaphylaxis")
        assertEquals(listOf(8 until 9, 12 until 13), text.ranges(SearchTerm("a")))
        assertFalse(SearchText("Hypoxia").contains(SearchTerm("xi")))
        assertTrue(SearchText("Hypoxia").contains(SearchTerm("oxi")))
    }

    @Test
    fun `short words in a longer query also match only at the start of a word`() {
        // "Check SpO₂" in 4-1; 2-1 has "Check" but no word starting "sp"
        assertEquals(listOf("4-1"), search.search("check sp").map { it.guideline.code })
    }

    @Test
    fun `terms are folded, deduplicated and punctuation-only words dropped`() {
        assertEquals(listOf(SearchTerm("spo2"), SearchTerm("a")), SearchTerm.terms("SpO₂ - a A spo2"))
    }

    @Test
    fun `ranges map back to the original text`() {
        val text = SearchText("Check S­pO₂, then SPO2 again")
        assertEquals(listOf("S\u00adpO₂", "SPO2"), text.ranges(SearchTerm("spo2")).map { text.original.substring(it.first, it.last + 1) })
        assertEquals(6 until 11, text.firstRange(SearchTerm("spo2")))
        assertNull(text.firstRange(SearchTerm("fio2")))
    }

    @Test
    fun `highlight ranges cover every term, sorted`() {
        assertEquals(listOf(0 until 5, 6 until 9), highlightRanges("Start CPR now", SearchTerm.terms("cpr start")))
        assertTrue(highlightRanges("Start CPR now", emptyList()).isEmpty())
    }

    @Test
    fun `snippet of short text has no ellipses`() {
        val snippet = GuidelineSearch.extractSnippet("alpha beta gamma", 6, 4)

        assertEquals("alpha beta gamma", snippet.text)
        assertEquals("beta", snippet.text.substring(snippet.highlightStart, snippet.highlightEnd))
    }

    @Test
    fun `snippet of long text is ellipsised and trimmed to whole words`() {
        val text = (0 until 50).joinToString(" ") { "word$it" }
        val matchIndex = text.indexOf("word25")

        val snippet = GuidelineSearch.extractSnippet(text, matchIndex, "word25".length)

        assertTrue(snippet.text.startsWith("…"))
        assertTrue(snippet.text.endsWith("…"))
        assertEquals("word25", snippet.text.substring(snippet.highlightStart, snippet.highlightEnd))
        val inner = snippet.text.removePrefix("…").removeSuffix("…")
        assertTrue(inner.split(" ").all { it.matches(Regex("word\\d+")) })
    }
}
