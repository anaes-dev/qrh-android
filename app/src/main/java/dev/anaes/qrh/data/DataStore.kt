package dev.anaes.qrh.data

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.anaes.qrh.model.DataModel
import javax.inject.Inject

class DataStore @Inject constructor(@ApplicationContext val context: Context) {

    private fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION") getPackageInfo(packageName, flags)
        }



    fun getData(): DataModel {
        val filename = "data.json"
        val gson: Gson = Gson()
        val json: String = context.assets.open(filename).bufferedReader().use { it.readText() }
        val data: DataModel = gson.fromJson(json, DataModel::class.java)
        if(data.version != context.packageManager.getPackageInfoCompat(context.packageName,0).versionName) {
            throw Exception("Invalid Data Payload")
        }
        return data
    }
}