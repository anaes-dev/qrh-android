package dev.anaes.qrh

import dev.anaes.qrh.model.AppData
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Sanity checks on the guideline content actually shipped in the app. */
class BundledDataTest {

    // Unit tests run with the module directory as the working directory
    private val appData: AppData = Json { ignoreUnknownKeys = true }
        .decodeFromString(File("src/main/assets/data.json").readText())

    private val guidelineLink = Regex("→\\s?(\\d-\\d{1,2})")

    @Test
    fun `bundled data parses and is not empty`() {
        assertTrue(appData.data.isNotEmpty())
        assertTrue(appData.data.all { it.content.isNotEmpty() })
    }

    @Test
    fun `guideline codes are unique`() {
        val codes = appData.data.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
    }

    @Test
    fun `every cross-reference points to a bundled guideline`() {
        val codes = appData.data.map { it.code }.toSet()
        val broken = appData.data.flatMap { guideline ->
            guideline.content.flatMap { item ->
                guidelineLink.findAll(item.head + " " + item.body)
                    .map { it.groupValues[1] }
                    .filter { it !in codes }
                    .map { "${guideline.code} -> $it" }
            }
        }
        assertTrue("Broken guideline links: $broken", broken.isEmpty())
    }

    @Test
    fun `every content item has a known type`() {
        val unknown = appData.data.flatMap { guideline ->
            guideline.content.filter { it.type !in 1..12 }.map { "${guideline.code}: type ${it.type}" }
        }
        assertTrue("Unknown content types: $unknown", unknown.isEmpty())
    }
}
