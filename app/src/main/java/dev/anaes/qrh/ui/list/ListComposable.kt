package dev.anaes.qrh.ui.list


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import dev.anaes.qrh.vm.DataViewModel

@Composable
    fun ListComposable(
        viewModel: DataViewModel,
        loadDetail: (String) -> Unit
    ) {
        val scrollState = rememberLazyListState()


//    val grouped = viewModel.filteredList.value.groupBy { it.code[0] }


    Column {
            ListSearch(viewModel.searchError.value, viewModel.searchString.value) {
                viewModel.updateSearch(it)
            }
            LazyColumn(state = scrollState) {
                items(viewModel.filteredList.value) { item->
                    ListItem(
                        headlineContent = { Text(item.titleA) },
                        overlineContent = { Text(item.codeA) },
                        trailingContent = { Text("v.${item.version}") },
                        supportingContent = { if(item.bodySearchA.isNotEmpty()) {
                            Text(AnnotatedString("...") + item.bodySearchA + AnnotatedString("..."))
                        } },
                        modifier = Modifier
                            .selectable(
                                selected = false,
                                onClick = { loadDetail(item.code) }
                            )
                    )
                }



/*                grouped.forEach { (initial, items ) ->
                    stickyHeader {
                        Text(
                            text = "Section ${initial.toString()}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.LightGray)
                                .padding(6.dp)
                        )
                        Divider()
                    }

                    items(items) { item ->
                        ListItem(
                            text = { Text(item.titleA) },
                            overlineText = { Text(item.codeA) },
                            trailing = { Text("v.${item.version.toString()}") },
                            modifier = Modifier
                                .selectable(
                                    selected = false,
                                    onClick = { loadDetail(item.code) }
                                )
                        )

                        Divider()

                    }
                }*/


            }
        }

    }


