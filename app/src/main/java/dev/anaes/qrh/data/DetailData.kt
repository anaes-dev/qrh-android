package dev.anaes.qrh.data

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.anaes.qrh.model.DetailModel
import javax.inject.Inject

class DetailData @Inject constructor(@ApplicationContext val context: Context) {

    private var _parsedGuidelines = mutableMapOf<String, Int>()

    private var _guidelines: ArrayList<DetailModel> = ArrayList()

    fun getDetailData(code: String): DetailModel? {
        var data: DetailModel? = null
        if (_parsedGuidelines.contains(code)) {
            val index = _parsedGuidelines[code] as Int
            index?.let {
                data = _guidelines[index]
            }
        } else {
            val filename = "$code.json"
            data = getFromFile(filename, context)
            _guidelines.add(data!!)
            _parsedGuidelines[code] = _guidelines.lastIndex
        }
        return data
    }

    private fun getFromFile(filename: String, context: Context): DetailModel {
        val gson: Gson = Gson()
        val json = context.assets.open(filename).bufferedReader().use { it.readText() }
        return gson.fromJson(json, DetailModel::class.java)
    }

}
