package dev.anaes.qrh.vm

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anaes.qrh.data.DetailData
import dev.anaes.qrh.model.DetailModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: DetailData
    ): ViewModel() {

    val testData: DetailModel? = repository.getDetailData("1-1")
}