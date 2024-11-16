package dev.anaes.qrh.vm
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anaes.qrh.data.DataStore
import dev.anaes.qrh.model.Guideline
import dev.anaes.qrh.model.ListGuideline
import org.jsoup.Jsoup
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(
    private val repository: DataStore,
) : ViewModel() {

    var data: List<Guideline> = listOf()
    private var unfilteredList: List<ListGuideline> = listOf()
    var filteredList: MutableState<List<ListGuideline>> = mutableStateOf(listOf())
    var searchString: MutableState<String> = mutableStateOf("")
    var searchError: MutableState<Boolean> = mutableStateOf(false)
    var onList: MutableState<Boolean> = mutableStateOf(true)

    var backStack: MutableList<String> = mutableListOf()

    init {
        data = repository.getData().guidelines
        unfilteredList = data.map { guideline ->
            ListGuideline(
                title = guideline.title,
                code = guideline.code,
                version = guideline.version,
                url = guideline.url,
                content = guideline.content.map { content -> Jsoup.parse(content.body).text() },
                titleA = AnnotatedString(guideline.title),
                codeA = AnnotatedString(guideline.code),
                bodySearchA = AnnotatedString("")
            )
        }
        filteredList = mutableStateOf(unfilteredList)
    }

    fun addBackStack(entry: String) {
        if(!backStack.contains(entry)) {
            backStack = backStack.apply { add(entry) }
        }
    }

    fun popBackStack(entry: String?) {
        backStack = backStack.subList(0, backStack.indexOfFirst { it -> it == entry })
    }

    fun updateBackStack(entry: String) {
        if(backStack.isEmpty() || !backStack.contains(entry)) {
            backStack = backStack.apply { add(entry) }
        } else {
            backStack = backStack.subList(0, backStack.indexOfFirst { it -> it == entry } + 1)
        }
    }

    fun clearBackStack() {
        backStack = mutableListOf()
    }

    fun updateSearch(query: String) {

        val cleanQuery: String = query.replace("\n", "")

        val codeQuery: String = when (cleanQuery.length) {
            1 ->
                "$cleanQuery-"
            in 2..4 ->
                if (cleanQuery.subSequence(1,1) == "-") {
                    cleanQuery
                } else {
                    cleanQuery[0] + "-" + cleanQuery.subSequence(1, cleanQuery.length)
                }
            else ->
                cleanQuery
        }

        filteredList.value = unfilteredList.filter {
            ((it.title.contains(cleanQuery, true))
                    or (it.code.contains(codeQuery, true))
                    or (cleanQuery.length > 2 && it.content.toString().contains(cleanQuery, ignoreCase = true))
            )
        }

        filteredList.value.forEach { item ->
            item.titleA = AnnotatedString(item.title)
            item.codeA = AnnotatedString(item.code)
            item.bodySearchA = AnnotatedString("")

            if (cleanQuery.isNotEmpty()) {

                val regex = ("(?i)$cleanQuery").toRegex()
                val codeRegex = ("(?i)$codeQuery").toRegex()

                val titleMatches = regex.findAll(item.title).map { it.range }.toList()
                val codeMatches = codeRegex.findAll(item.code).map { it.range }.toList()

                val titleOutput = AnnotatedString.Builder(item.title)
                val codeOutput = AnnotatedString.Builder(item.code)

                titleMatches.forEach { match ->
                    titleOutput.apply {
                        addStyle(
                            SpanStyle(background = Color.Red.copy(alpha = 0.5F)),
                            match.first,
                            match.last + 1
                        )
                    }
                }

                item.titleA = titleOutput.toAnnotatedString()

                codeMatches.forEach { match ->
                    codeOutput.apply {
                        addStyle(
                            SpanStyle(background = Color.Red.copy(alpha = 0.5F)),
                            match.first,
                            match.last + 1
                        )
                    }

                }

                item.codeA = codeOutput.toAnnotatedString()


                val bodyMatches =
                    codeRegex.findAll(item.content.toString()).map { it.range }.toList()

                if(cleanQuery.length > 3 && bodyMatches.isNotEmpty()) {
                    item.bodySearchA = AnnotatedString(item.content.toString())
                    val bodyOutput = AnnotatedString.Builder(item.content.toString())

                        bodyOutput.apply {
                            addStyle(
                                SpanStyle(background = Color.Red.copy(alpha = 0.5F)),
                                bodyMatches.first().first,
                                bodyMatches.first().last + 1
                            )
                        }

                        item.bodySearchA = bodyOutput.toAnnotatedString().subSequence(
                            (bodyMatches.first().first - 20).coerceAtLeast(0),
                            (bodyMatches.first().last + 20).coerceAtMost(bodyMatches.first().first + 60).coerceAtMost(item.bodySearchA.length)
                        )
                    }



            }
        }
        searchString.value = cleanQuery
        searchError.value = filteredList.value.isEmpty()
    }
}