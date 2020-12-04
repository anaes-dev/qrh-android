package dev.anaes.qrh

import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil

class MainViewModel : ViewModel() {

    private var _breadcrumbTitles: MutableMap<Int, String> = HashMap()

    private var _darkDisabled: Boolean = false

    fun getBreadCrumbTitle(index: Int): String {
        return if (_breadcrumbTitles[index] != null) {
            _breadcrumbTitles[index].toString()
        } else {
            "Error"
        }
    }

    fun setBreadCrumbTitle(title: String, index: Int) {
        _breadcrumbTitles[index] = title
    }

    fun checkDarkDisabled(): Boolean {
        return _darkDisabled
    }

    fun darkDisabled(disabled: Boolean) {
        _darkDisabled = disabled
    }

    var isStartup: Boolean = true
    var isDarkMode: Boolean = false

}