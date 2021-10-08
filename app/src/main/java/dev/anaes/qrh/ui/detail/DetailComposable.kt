package dev.anaes.qrh.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Divider
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.anaes.qrh.vm.ListViewModel

@Composable
fun DetailComposable(
    viewModel: ListViewModel,
    code: String
) {
    val scrollState = rememberLazyListState()

    LazyColumn(state = scrollState) {

        viewModel.detailDataObject.value[code].let {
            items(it!!) { item ->
                Column() {
                    DetailItem(
                        item.type,
                        item.step,
                        item.head,
                        item.body,
                        item.collapsed
                    )
                }
            }
        }


}
}