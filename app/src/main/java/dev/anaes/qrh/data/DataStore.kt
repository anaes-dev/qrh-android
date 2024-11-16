package dev.anaes.qrh.data
import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.anaes.qrh.model.DataModel
import javax.inject.Inject

class DataStore @Inject constructor(@ApplicationContext val context: Context) {
    fun getData(): DataModel {
        val filename = "data.json"
        val gson: Gson = Gson()
        val json: String = context.assets.open(filename).bufferedReader().use { it.readText() }
        val data: DataModel = gson.fromJson(json, DataModel::class.java)
        return data
    }
}