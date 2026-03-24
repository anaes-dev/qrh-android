package dev.anaes.qrh

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.anaes.qrh.data.GuidelineRepository
import dev.anaes.qrh.model.Guideline

class QrhViewModel(
    val repository: GuidelineRepository,
) : ViewModel() {

    val guidelines: List<Guideline> = repository.guidelines

    var searchQuery by mutableStateOf("")
        private set

    var isStartup by mutableStateOf(true)
        private set

    fun onSearchQueryChange(query: String) {
        searchQuery = query
    }

    fun onStartupComplete() {
        isStartup = false
    }

    val filteredGuidelines: List<Guideline>
        get() {
            if (searchQuery.isBlank()) return guidelines
            val query = searchQuery.trim().lowercase()
            return guidelines.filter { guideline ->
                val searchable = "${guideline.code} ${guideline.title}".lowercase()
                searchable.contains(query)
            }
        }

    fun getGuideline(code: String): Guideline? = repository.getGuideline(code)
}
