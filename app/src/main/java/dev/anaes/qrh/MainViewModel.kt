package dev.anaes.qrh

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var breadcrumbTitles: MutableMap<Int, String> = HashMap()
    var isStartup: Boolean = true
}