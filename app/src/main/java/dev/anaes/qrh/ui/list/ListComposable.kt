package dev.anaes.qrh.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.material.ListItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.R
import dev.anaes.qrh.vm.ListViewModel


@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
    fun ListComposable(
        viewModel: ListViewModel,
        loadDetail: (String) -> Unit
    ) {
        val scrollState = rememberLazyListState()


//    val grouped = viewModel.filteredList.value.groupBy { it.code[0] }


    Column {
            ListSearch(viewModel.searchError.value, viewModel.searchString.value) {
                viewModel.updateSearch(it)
            }
            LazyColumn(state = scrollState) {

                    items(viewModel.filteredList.value) { item ->
                        ListItem(
                            text = { Text(item.titleA) },
                            overlineText = { Text(item.codeA) },
                            trailing = { Text("v.${item.version}") },
                            modifier = Modifier
                                .selectable(
                                    selected = false,
                                    onClick = { loadDetail(item.code) }
                                )
                        )

                        Divider()
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


