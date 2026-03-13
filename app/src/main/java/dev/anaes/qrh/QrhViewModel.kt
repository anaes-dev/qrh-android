package dev.anaes.qrh

import android.text.Html
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.anaes.qrh.data.GuidelineRepository
import dev.anaes.qrh.model.Guideline
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class SearchResult(
    val guideline: Guideline,
    val snippet: String? = null,
    val matchStart: Int = -1,
    val matchEnd: Int = -1,
)

class QrhViewModel(
    val repository: GuidelineRepository,
) : ViewModel() {

    val guidelines: List<Guideline> = repository.guidelines

    var searchQuery by mutableStateOf("")
        private set

    var isStartup by mutableStateOf(true)
        private set

    var searchResults by mutableStateOf<List<SearchResult>>(emptyList())
        private set

    private var searchJob: Job? = null

    // Pre-compute plain text content for each guideline for search
    private val guidelineTextCache: Map<String, String> by lazy {
        guidelines.associate { guideline ->
            guideline.code to guideline.content.joinToString(" ") { item ->
                val headText = if (item.head.isNotBlank()) stripHtml(item.head) else ""
                val bodyText = if (item.body.isNotBlank()) stripHtml(item.body) else ""
                "$headText $bodyText"
            }.replace("\\s+".toRegex(), " ").trim()
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        searchJob?.cancel()
        if (query.isBlank()) {
            searchResults = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(250) // debounce
            searchResults = performSearch(query.trim())
        }
    }

    fun onStartupComplete() {
        isStartup = false
    }

    val filteredGuidelines: List<Guideline>
        get() {
            if (searchQuery.isBlank()) return guidelines
            return searchResults.map { it.guideline }
        }

    private fun performSearch(query: String): List<SearchResult> {
        val lowerQuery = query.lowercase()
        val results = mutableListOf<SearchResult>()

        for (guideline in guidelines) {
            val titleMatch = "${guideline.code} ${guideline.title}".lowercase()
            if (titleMatch.contains(lowerQuery)) {
                // Title/code match — no snippet needed
                results.add(SearchResult(guideline))
                continue
            }

            // Search content
            val fullText = guidelineTextCache[guideline.code] ?: continue
            val lowerText = fullText.lowercase()
            val matchIndex = lowerText.indexOf(lowerQuery)
            if (matchIndex >= 0) {
                val snippet = extractSnippet(fullText, matchIndex, lowerQuery.length)
                results.add(SearchResult(
                    guideline = guideline,
                    snippet = snippet.text,
                    matchStart = snippet.highlightStart,
                    matchEnd = snippet.highlightEnd,
                ))
            }
        }

        return results
    }

    private data class Snippet(
        val text: String,
        val highlightStart: Int,
        val highlightEnd: Int,
    )

    private fun extractSnippet(text: String, matchIndex: Int, matchLength: Int): Snippet {
        val contextChars = 40
        val snippetStart = (matchIndex - contextChars).coerceAtLeast(0)
        val snippetEnd = (matchIndex + matchLength + contextChars).coerceAtMost(text.length)

        // Adjust to word boundaries
        val adjustedStart = if (snippetStart > 0) {
            val spaceIndex = text.indexOf(' ', snippetStart)
            if (spaceIndex in snippetStart until matchIndex) spaceIndex + 1 else snippetStart
        } else snippetStart

        val adjustedEnd = if (snippetEnd < text.length) {
            val spaceIndex = text.lastIndexOf(' ', snippetEnd)
            if (spaceIndex > matchIndex + matchLength) spaceIndex else snippetEnd
        } else snippetEnd

        val prefix = if (adjustedStart > 0) "\u2026" else ""
        val suffix = if (adjustedEnd < text.length) "\u2026" else ""

        val snippetText = prefix + text.substring(adjustedStart, adjustedEnd) + suffix
        val highlightStart = prefix.length + (matchIndex - adjustedStart)
        val highlightEnd = highlightStart + matchLength

        return Snippet(snippetText, highlightStart, highlightEnd)
    }

    fun getGuideline(code: String): Guideline? = repository.getGuideline(code)

    companion object {
        private fun stripHtml(html: String): String {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
        }
    }
}
