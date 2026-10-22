package dev.anaes.qrh.search

import dev.anaes.qrh.model.ContentItem
import dev.anaes.qrh.model.Guideline
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GuidelineSearchTest {

    private val tagRegex = Regex("<[^>]*>")
    private val stripTags: (String) -> String = { it.replace(tagRegex, "").trim() }

    private fun guideline(code: String, title: String, vararg bodies: String) = Guideline(
        title = title,
        code = code,
        version = 1,
        url = "https://example.com/$code.pdf",
        content = bodies.map { ContentItem(type = 3, body = it) },
    )

    private val guidelines = listOf(
        guideline("1-1", "Cardiac arrest", "Start <b>CPR</b> and call for help"),
        guideline("2-1", "Hypoxia", "Increase FiO2 to 1.0", "Check the <i>breathing circuit</i>"),
        guideline("3-4", "Anaphylaxis", "Give adrenaline IM; consider cardiac arrest protocol"),
    )

    private val search = GuidelineSearch(guidelines, stripTags)

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
        assertEquals(0, result.titleMatchStart)
        assertEquals(5, result.titleMatchEnd)
        assertNull(result.snippet)
    }

    @Test
    fun `code match is found without a title highlight`() {
        val result = search.search("3-4").single()

        assertEquals("3-4", result.guideline.code)
        assertEquals(-1, result.titleMatchStart)
        assertEquals(-1, result.titleMatchEnd)
    }

    @Test
    fun `content match returns a snippet highlighting the query`() {
        val result = search.search("adrenaline").single()

        assertEquals("3-4", result.guideline.code)
        val snippet = result.snippet!!
        assertEquals("adrenaline", snippet.substring(result.matchStart, result.matchEnd))
    }

    @Test
    fun `title match takes precedence over a content match in the same guideline`() {
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
        assertEquals("Start CPR", result.snippet!!.substring(result.matchStart, result.matchEnd))
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
    fun `results keep list order`() {
        val results = search.search("a")
        assertEquals(listOf("1-1", "2-1", "3-4"), results.map { it.guideline.code })
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
