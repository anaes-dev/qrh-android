package dev.anaes.qrh.ui.list

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

    @Composable
    fun ListComposable() {
        // We save the scrolling position with this state that can also
        // be used to programmatically scroll the list
        val scrollState = rememberLazyListState()

        LazyColumn(state = scrollState) {
            items(100) {
                Text("Item #$it")
            }
        }
    }
