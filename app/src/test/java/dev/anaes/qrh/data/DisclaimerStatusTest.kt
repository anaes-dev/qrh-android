package dev.anaes.qrh.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DisclaimerStatusTest {

    private val current = 48

    @Test
    fun `fresh install shows first-run disclaimers`() {
        assertEquals(
            DisclaimerStatus.FIRST_RUN,
            disclaimerStatusFor(seenWarning = false, legacySeenWarning = false, savedVersion = 0, currentVersion = current),
        )
    }

    @Test
    fun `accepted for the current version goes straight to the app`() {
        assertEquals(
            DisclaimerStatus.ACCEPTED,
            disclaimerStatusFor(seenWarning = true, legacySeenWarning = false, savedVersion = current, currentVersion = current),
        )
    }

    @Test
    fun `accepted for a newer version (downgrade) goes straight to the app`() {
        assertEquals(
            DisclaimerStatus.ACCEPTED,
            disclaimerStatusFor(seenWarning = true, legacySeenWarning = false, savedVersion = current + 1, currentVersion = current),
        )
    }

    @Test
    fun `accepted for an older version shows the updated disclaimers`() {
        // e.g. upgrading from v2.17 (versionCode 47), with the value migrated from SharedPreferences
        assertEquals(
            DisclaimerStatus.UPDATED,
            disclaimerStatusFor(seenWarning = true, legacySeenWarning = false, savedVersion = 47, currentVersion = current),
        )
    }

    @Test
    fun `accepted only under the old package name shows the updated disclaimers`() {
        assertEquals(
            DisclaimerStatus.UPDATED,
            disclaimerStatusFor(seenWarning = false, legacySeenWarning = true, savedVersion = 0, currentVersion = current),
        )
    }
}
