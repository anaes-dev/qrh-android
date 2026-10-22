package dev.anaes.qrh.data

import android.content.res.AssetManager
import android.util.Log
import dev.anaes.qrh.model.AppData
import dev.anaes.qrh.model.Guideline
import dev.anaes.qrh.model.GuidelineCopyright
import kotlinx.serialization.json.Json

class GuidelineRepository(private val assets: AssetManager) {

    private val json = Json { ignoreUnknownKeys = true }

    // Synchronized lazy: parsed once, on whichever thread asks first (normally the
    // background preload started by QrhApplication).
    private val appData: AppData by lazy {
        try {
            assets.open("data.json").bufferedReader().use { reader ->
                json.decodeFromString<AppData>(reader.readText())
            }
        } catch (e: Exception) {
            Log.e("GuidelineRepository", "Failed to load guidelines data", e)
            AppData(version = "0", data = emptyList())
        }
    }

    private val byCode: Map<String, Guideline> by lazy { guidelines.associateBy { it.code } }

    val guidelines: List<Guideline> get() = appData.data

    val version: String get() = appData.version

    /** Forces the JSON to be parsed now; call from a background thread. */
    fun preload() {
        byCode
    }

    fun getGuideline(code: String): Guideline? = byCode[code]

    /** The guideline's copyright with its holder resolved, or null if absent or unknown. */
    fun getCopyright(guideline: Guideline): GuidelineCopyright? {
        val copyright = guideline.copyright ?: return null
        val holder = appData.copyrightHolders[copyright.holder] ?: return null
        return GuidelineCopyright(holder.name, holder.website, copyright.year)
    }
}
