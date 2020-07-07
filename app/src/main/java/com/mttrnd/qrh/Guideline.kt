package com.mttrnd.qrh

import android.content.Context
import android.view.View
import org.json.JSONException
import org.json.JSONObject


class Guideline(
    val title: String,
    val codedisplay: String,
    val version: Int) {

    companion object {

        fun getGuidelinesFromFile(filename: String, context: Context): ArrayList<Guideline> {
            val guidelineList = ArrayList<Guideline>()

            try {
                val jsonString = loadJsonFromAsset("guidelines.json", context)
                val json = JSONObject(jsonString)
                val guidelines = json.getJSONArray("guidelines")

                (0 until guidelines.length()).mapTo(guidelineList) {
                    Guideline(
                        guidelines.getJSONObject(it).getString("title"),
                        guidelines.getJSONObject(it).getString("codedisplay"),
                        guidelines.getJSONObject(it).getInt("version")
                    )
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return guidelineList
        }

        private fun loadJsonFromAsset(filename: String, context: Context): String? {
            var json: String? = null

            try {
                val inputStream = context.assets.open(filename)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                json = String(buffer, Charsets.UTF_8)
            } catch (ex: java.io.IOException) {
                ex.printStackTrace()
                return null
            }

            return json
        }
    }
}