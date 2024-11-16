package dev.anaes.qrh.ui.detail

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import dev.anaes.qrh.model.Guideline
import dev.anaes.qrh.ui.theme.tertiaryLight
import dev.anaes.qrh.vm.DataViewModel
import java.util.regex.Pattern

@OptIn(ExperimentalTextApi::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun DetailComposable(
    viewModel: DataViewModel,
    guideline: Guideline,
    code: String,
    navController: NavController,
    loadDetail: (String) -> Unit,
    loadList: () -> Unit,
) {
    val scrollState = rememberLazyListState()

    Log.d("Backstack:", viewModel.backStack.toString())

    BackHandler(enabled = true, onBack = {
        if (viewModel.backStack.count() > 1) {
            val backCode: String = viewModel.backStack[viewModel.backStack.lastIndex - 1]
            loadDetail(backCode)
        } else {
            loadList()
        }
    })

    Column(Modifier.fillMaxHeight()) {
        FlowRow(
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.Center
        ) {
//            Button(onClick = { loadList() }) {
//                Text("Home")
//            }
            AssistChip(onClick = { loadList() }, label = { Text("Home") }, leadingIcon = { Icon(Icons.Filled.Home, "Home") } )
            viewModel.backStack.forEach { item ->
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Arrow", modifier = Modifier.align(alignment = Alignment.CenterVertically))
                SuggestionChip(
                    onClick = { loadDetail(item) },
                    label = { Text(viewModel.data.single { it.code == item }.title)},
                    modifier = Modifier.padding(all = Dp(0f))
                )

//                Button(onClick = { loadDetail(item) }) {
//                    Text(text = item)
//                    Text(text = viewModel.data.single { it.code == item }.title)
//                }
            }
        }

        LazyColumn(Modifier.weight(1f), state = scrollState) {
            items(guideline.content) { item ->
                Column {
                    Text(item.type.toString())
                    Text(item.step.toString())
                    Text(item.head.toString())

                    val regex = Pattern.compile("[(]?[→][\\s]?[1-4][-][0-9]{1,2}[)]?").toRegex()
                    val matches = regex.findAll(item.body).map { matches -> matches.range }.toList()
                    val bodyOutput = AnnotatedString.Builder(item.body)

                    matches.forEach { match ->
                        bodyOutput.apply {
                            addUrlAnnotation(
                                UrlAnnotation(url = item.body.substring(match).replace("→", "")),
                                match.first,
                                match.last + 1
                            )
                            addStyle(
                                SpanStyle(
                                    color = tertiaryLight,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                ),
                                match.first,
                                match.last + 1
                            )
                        }
                    }

                    ClickableText(
                        text = bodyOutput.toAnnotatedString(),
                        onHover = {},
                        onClick = { offset ->
                            bodyOutput.toAnnotatedString().getUrlAnnotations(
                                start = offset, end = offset
                            ).firstOrNull()?.let { annotation ->
                                // If yes, we log its value
                                val linkCode = annotation.item.url
                                loadDetail(linkCode)
                            }
                        })

                }
            }
        }


    }

//
//        viewModel.data.single { it.code.toString().lowercase() == code.toString().lowercase() }.content.forEach {
//            item ->
//            DetailItem(
//            item.type,
//            item.step,
//            item.head,
//            item.body,
//            item.collapsed
//        )
//        }

}