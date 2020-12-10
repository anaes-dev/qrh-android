package dev.anaes.qrh

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {

    var breadcrumbTitles: HashMap<Int, String> = HashMap()

    private var _darkDisabled: Boolean = false

    var context: Context = application.applicationContext

    var isStartup: Boolean = true
    var isDarkMode: Boolean = false

    fun getBreadCrumbTitle(index: Int): String {
        return if (breadcrumbTitles[index] != null) {
            breadcrumbTitles[index].toString()
        } else {
            "Error"
        }
    }

    fun setBreadCrumbTitle(title: String, index: Int) {
        breadcrumbTitles[index] = title
    }

    fun checkDarkDisabled(): Boolean {
        return _darkDisabled
    }

    fun darkDisabled(disabled: Boolean) {
        _darkDisabled = disabled
    }



    private var list: ArrayList<Guideline> = Guideline.getGuidelinesFromFile("guidelines.json", context)

    fun getGuidelineList(): ArrayList<Guideline> {
        return list
    }



    private var _parsedGuidelines = mutableMapOf<String, Int>()

    private var _guidelines: ArrayList<ArrayList<DetailContent>> = ArrayList()

    fun fetchGuideline(code: String): ArrayList<DetailContent> {
        var data = ArrayList<DetailContent>()
        if (_parsedGuidelines.contains(code)) {
            val index = _parsedGuidelines[code] as Int
            index?.let {
                data = _guidelines[index]
            }
        } else {
            val filename = "$code.json"
            data = DetailContent.getContentFromFile(filename, context)
            _guidelines.add(data)
            _parsedGuidelines[code] = _guidelines.lastIndex
        }
        return data
    }

}