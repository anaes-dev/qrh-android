package dev.anaes.qrh

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var breadcrumbList: ArrayList<String> = ArrayList()
    var breadcrumbCount: Int = 0
    var breadcrumbIsActive: Boolean = false
    var seenWarning: Boolean = false
    var menuItem: Int = R.id.detail_code
}