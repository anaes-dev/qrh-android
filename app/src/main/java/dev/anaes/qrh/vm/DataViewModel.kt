package dev.anaes.qrh.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anaes.qrh.data.DataStore
import dev.anaes.qrh.model.Guideline
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(
    private val repository: DataStore,
) : ViewModel() {

    var data: List<Guideline> = listOf()

    init {

            data = repository.getData().guidelines

    }

}