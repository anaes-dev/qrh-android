package dev.anaes.qrh.ui.list

import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
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

@Composable
    fun ListComposable(viewModel: ListViewModel) {
        val scrollState = rememberLazyListState()


        viewModel.filteredList.value = viewModel.unfilteredList.filter {
            it.title.contains(viewModel.searchString.value.text, true)
        }

    Column {
            SearchView(viewModel)
            LazyColumn(state = scrollState) {

                    items(viewModel.filteredList.value) { item ->
                        Text(text = item.title)

                    }

            }
        }

    }


@Composable
fun SearchView(viewModel: ListViewModel) {
    val focusManager = LocalFocusManager.current

    val noResults: Boolean = viewModel.filteredList.value.isEmpty()


    val errorColor = if (noResults) {
        MaterialTheme.colors.error
    } else {
        MaterialTheme.colors.primary
    }

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            TextField(

                value = viewModel.searchString.value,

                onValueChange = { value ->
                    viewModel.searchString.value = value
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
                    if (viewModel.searchString.value != TextFieldValue("")) {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.searchString.value = TextFieldValue("")
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
            if (noResults) {
                Text(
                    text = "No results",
                    style = MaterialTheme.typography.caption.copy(color = errorColor),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }