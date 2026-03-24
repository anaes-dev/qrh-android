package dev.anaes.qrh

import android.app.Application
import dev.anaes.qrh.data.GuidelineRepository
import dev.anaes.qrh.data.UserPreferences
import kotlin.concurrent.thread

/**
 * Holds app-wide singletons so they survive Activity recreation (rotation, theme change)
 * instead of being rebuilt — and the guideline JSON re-parsed — every time.
 */
class QrhApplication : Application() {

    val repository: GuidelineRepository by lazy { GuidelineRepository(assets) }

    val preferences: UserPreferences by lazy { UserPreferences(this) }

    override fun onCreate() {
        super.onCreate()
        // Parse data.json off the main thread while the UI waits on DataStore. If the UI
        // needs the data first, it simply waits on the same synchronized lazy.
        thread(name = "qrh-preload") { repository.preload() }
    }
}
