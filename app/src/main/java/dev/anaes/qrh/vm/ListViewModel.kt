package dev.anaes.qrh.vm

import android.util.Log
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anaes.qrh.data.ListData
import dev.anaes.qrh.model.ListItem
import javax.inject.Inject
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: ListData
): ViewModel() {

    private var unfilteredList: List<ListItem> = listOf()

    var filteredList: MutableState<List<ListItem>> = mutableStateOf(listOf())

    var searchString: MutableState<String> = mutableStateOf("")

    var searchError: MutableState<Boolean> = mutableStateOf(false)

    var onList: MutableState<Boolean> = mutableStateOf(true)

    init {
        unfilteredList = repository.getListData()
        filteredList = mutableStateOf(unfilteredList)
        filteredList.value.forEach {
            it.titleA = AnnotatedString(it.title)
            it.codeA = AnnotatedString(it.code)
        }
    }

    fun updateSearch(query: String) {

        val cleanQuery: String = query.replace("\n", "")

        val codeQuery: String = when (cleanQuery.length) {
            0 ->
                cleanQuery
            1 ->
                "$cleanQuery-"
            in 2..5 ->
                if (cleanQuery.subSequence(1,1) == "-") {
                    cleanQuery
                } else {
                    cleanQuery[0] + "-" + cleanQuery.subSequence(1, cleanQuery.length)
                }
            else ->
                cleanQuery
        }

        filteredList.value = unfilteredList.filter {
            ((it.title.contains(cleanQuery, true)) or (it.code.contains(codeQuery, true)))
        }

        filteredList.value.forEach { item ->
            item.titleA = AnnotatedString(item.title)
            item.codeA = AnnotatedString(item.code)

            if (cleanQuery.isNotEmpty()) {

                val regex = ("(?i)$cleanQuery").toRegex()
                val codeRegex = ("(?i)$codeQuery").toRegex()

                val titleMatches = regex.findAll(item.title).map { it.range }.toList()
                val codeMatches = codeRegex.findAll(item.code).map { it.range }.toList()

                titleMatches.forEach { match ->
                    val output = AnnotatedString.Builder(item.title)
                        .apply {
                            addStyle(
                                SpanStyle(background = Color.Red.copy(alpha = 0.5F)),
                                match.first,
                                match.last + 1
                            )
                        }
                        .toAnnotatedString()
                    item.titleA = output
                }

                codeMatches.forEach { match ->
                    val output = AnnotatedString.Builder(item.code)
                        .apply {
                            addStyle(
                                SpanStyle(background = Color.Red.copy(alpha = 0.5F)),
                                match.first,
                                match.last + 1
                            )
                        }
                        .toAnnotatedString()
                    item.codeA = output
                }


            }


        }


        searchString.value = cleanQuery

        searchError.value = filteredList.value.isEmpty()
    }
}

