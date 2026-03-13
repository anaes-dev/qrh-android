package dev.anaes.qrh.data

import android.content.res.AssetManager
import dev.anaes.qrh.model.AppData
import dev.anaes.qrh.model.Guideline
import kotlinx.serialization.json.Json

class GuidelineRepository(assets: AssetManager) {

    private val json = Json { ignoreUnknownKeys = true }

    private val appData: AppData = assets.open("data.json").bufferedReader().use { reader ->
        json.decodeFromString<AppData>(reader.readText())
    }

    val guidelines: List<Guideline> = appData.data

    val version: String = appData.version

    fun getGuideline(code: String): Guideline? =
        guidelines.find { it.code == code }
}
