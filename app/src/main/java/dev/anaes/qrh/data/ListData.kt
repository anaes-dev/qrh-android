package dev.anaes.qrh.data

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.anaes.qrh.model.ListItem
import dev.anaes.qrh.model.ListModel
import javax.inject.Inject

class ListData @Inject constructor(@ApplicationContext val context: Context) {

    fun getListData(): List<ListItem> {
        val filename = "guidelines.json"
        return getFromFile(filename, context).items
    }

    private fun getFromFile(filename: String, context: Context): ListModel {
        val gson: Gson = Gson()
        val json = context.assets.open(filename).bufferedReader().use { it.readText() }
        return gson.fromJson(json, ListModel::class.java)
    }

}
