package dev.anaes.qrh

import android.content.Context
import org.json.JSONException
import org.json.JSONObject


class Guideline(
    val title: String,
    val code: String,
    val version: Int,
    val url: String) {

    companion object {

        fun getGuidelinesFromFile(filename: String, context: Context): ArrayList<Guideline> {
            val guidelineList = ArrayList<Guideline>()

            try {
                val jsonString =
                    loadJsonFromAsset(
                        filename,
                        context
                    )
                val json = JSONObject(jsonString)
                val guidelines = json.getJSONArray("guidelines")

                (0 until guidelines.length()).mapTo(guidelineList) {
                    Guideline(
                        guidelines.getJSONObject(it).getString("title"),
                        guidelines.getJSONObject(it).getString("code"),
                        guidelines.getJSONObject(it).getInt("version"),
                        guidelines.getJSONObject(it).getString("url")
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