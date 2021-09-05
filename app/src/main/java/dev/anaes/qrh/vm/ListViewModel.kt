package dev.anaes.qrh.vm

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anaes.qrh.data.ListData
import dev.anaes.qrh.model.ListItem
import dev.anaes.qrh.model.ListModel
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: ListData
): ViewModel() {

    var unfilteredList: List<ListItem> = listOf()

    var filteredList: MutableState<List<ListItem>> = mutableStateOf(listOf())

    var searchString: MutableState<TextFieldValue> = mutableStateOf(TextFieldValue())

    var onList: MutableState<Boolean> = mutableStateOf(true)

    init {
        unfilteredList = repository.getListData()
        filteredList = mutableStateOf(unfilteredList)
    }



}