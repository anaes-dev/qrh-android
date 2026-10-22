package dev.anaes.qrh

import android.text.Html
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.anaes.qrh.data.GuidelineRepository
import dev.anaes.qrh.model.Guideline
import dev.anaes.qrh.model.GuidelineCopyright
import dev.anaes.qrh.search.GuidelineSearch
import dev.anaes.qrh.search.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QrhViewModel(
    private val repository: GuidelineRepository,
    /** Keeps the search query if the system ends the app in the background. */
    private val savedState: SavedStateHandle,
) : ViewModel() {

    val guidelines: List<Guideline> get() = repository.guidelines

    var searchQuery by mutableStateOf("")
        private set

    var isStartup by mutableStateOf(true)
        private set

    var searchResults by mutableStateOf<List<SearchResult>>(emptyList())
        private set

    var isSearching by mutableStateOf(false)
        private set

    private var searchJob: Job? = null

    // Built (and its index populated) lazily on the first search, on a background thread
    private val search by lazy { GuidelineSearch(repository.guidelines, ::stripHtml) }

    init {
        savedState.get<String>(KEY_SEARCH_QUERY)?.let(::onSearchQueryChange)
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        savedState[KEY_SEARCH_QUERY] = query
        searchJob?.cancel()
        if (query.isBlank()) {
            searchResults = emptyList()
            isSearching = false
            return
        }
        isSearching = true
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            val results = withContext(Dispatchers.Default) { search.search(query) }
            searchResults = results
            isSearching = false
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

    fun getGuideline(code: String): Guideline? = repository.getGuideline(code)

    fun getCopyright(guideline: Guideline): GuidelineCopyright? = repository.getCopyright(guideline)

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 150L
        private const val KEY_SEARCH_QUERY = "searchQuery"

        private fun stripHtml(html: String): String {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
        }
    }
}
