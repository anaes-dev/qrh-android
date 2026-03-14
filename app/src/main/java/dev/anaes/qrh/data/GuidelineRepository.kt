package dev.anaes.qrh.data

import android.content.res.AssetManager
import android.util.Log
import dev.anaes.qrh.model.AppData
import dev.anaes.qrh.model.Guideline
import kotlinx.serialization.json.Json

class GuidelineRepository(assets: AssetManager) {

    private val json = Json { ignoreUnknownKeys = true }

    private val appData: AppData = try {
        assets.open("data.json").bufferedReader().use { reader ->
            json.decodeFromString<AppData>(reader.readText())
        }
    } catch (e: Exception) {
        Log.e("GuidelineRepository", "Failed to load guidelines data", e)
        AppData(version = "0", data = emptyList())
    }

    val guidelines: List<Guideline> = appData.data

    val version: String = appData.version

    fun getGuideline(code: String): Guideline? =
        guidelines.find { it.code == code }
}
