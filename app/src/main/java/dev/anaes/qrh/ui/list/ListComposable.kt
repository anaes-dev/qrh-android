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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.anaes.qrh.vm.ListViewModel

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
    fun ListComposable(viewModel: ListViewModel) {
        val scrollState = rememberLazyListState()


    val grouped = viewModel.filteredList.value.groupBy { it.code[0] }



    Column {
            SearchView(viewModel.searchError.value, viewModel.searchString.value) {
                viewModel.updateSearch(it)
            }
            LazyColumn(state = scrollState) {
                grouped.forEach { (initial, items ) ->
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
                                    onClick = { }
                                )
                        )

                        Divider()

                    }
                }


            }
        }

    }


@Composable
fun SearchView(
    searchError: Boolean,
    searchValue: String,
    newSearchValue: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    val errorColor = if (searchError) {
        MaterialTheme.colors.error
    } else {
        MaterialTheme.colors.primary
    }

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            TextField(

                isError = searchError,

                value = searchValue,

                onValueChange = { value ->
                    newSearchValue(value)
                },

                label = {
                    Text(
                        text = "Search",
                        style = TextStyle(color = errorColor)
                    )
                },

                modifier = Modifier
                    .fillMaxWidth(),

                textStyle = TextStyle(color = MaterialTheme.colors.onSurface, fontSize = 18.sp),

                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "",
                        modifier = Modifier
                            .padding(15.dp)
                            .size(24.dp)

                    )
                },

                trailingIcon = {
                    if (searchValue.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                newSearchValue("")
                            }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "",
                                modifier = Modifier
                                    .padding(15.dp)
                                    .size(24.dp)
                            )
                        }
                    }
                },

                singleLine = true,

                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),

                keyboardActions = KeyboardActions(
                    onSearch = { focusManager.clearFocus() }
                    ),

                shape = RectangleShape,

                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.DarkGray,
                    cursorColor = Color.LightGray,
                    leadingIconColor = Color.DarkGray,
                    trailingIconColor = Color.DarkGray,
                    focusedIndicatorColor = errorColor,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            if (searchError) {
                Text(
                    text = "No results",
                    style = MaterialTheme.typography.caption.copy(color = errorColor),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }