package dev.anaes.qrh.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the app's baseline profile: the code used launching the app and going through the
 * main journeys, which is then compiled ahead of time when the app is installed, so it's quick
 * from the first use.
 *
 * Run `./gradlew :app:generateBaselineProfile` with a device (API 33+, or rooted) connected. The
 * profile is written to `app/src/release/generated/baselineProfiles`, to commit with the app.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    /** Launching, which is also the app's startup profile: the code ordered first in the APK. */
    @Test
    fun startup() = rule.collect(
        packageName = targetPackage,
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
        acceptDisclaimers()
    }

    /**
     * The main journeys. The profile is the last pass's, so more passes only give it longer to
     * settle: capped below the default 15, as a slow emulator can take a minute a pass.
     */
    @Test
    fun journeys() = rule.collect(packageName = targetPackage, maxIterations = 5) {
        pressHome()
        startActivityAndWait()
        acceptDisclaimers()

        browseList()
        readGuideline()
        searchGuidelines()
    }
}
