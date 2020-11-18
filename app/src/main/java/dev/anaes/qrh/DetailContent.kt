package dev.anaes.qrh

import android.content.Context
import org.json.JSONException
import org.json.JSONObject


class DetailContent(
    val type: Int,
    val step: String,
    val head: String,
    val body: String,
    var collapsed: Boolean) {


    // Load content from guideline file into ArrayList

    companion object {

        fun getContentFromFile(filename: String, context: Context): ArrayList<DetailContent> {
            val contentData = ArrayList<DetailContent>()

            try {
                val jsonString =
                    loadJsonFromAsset(
                        filename,
                        context
                    )
                val json = JSONObject(jsonString)
                val contentCards = json.getJSONArray("DetailContent")

                (0 until contentCards.length()).mapTo(contentData) {
                    DetailContent(
                        contentCards.getJSONObject(it).getInt("type"),
                        contentCards.getJSONObject(it).getString("step"),
                        contentCards.getJSONObject(it).getString("head"),
                        contentCards.getJSONObject(it).getString("body"),
                        collapsed = true
                    )
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return contentData
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