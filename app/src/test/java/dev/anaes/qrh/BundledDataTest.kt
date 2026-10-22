package dev.anaes.qrh

import dev.anaes.qrh.model.AppData
import dev.anaes.qrh.model.ContentBlock
import dev.anaes.qrh.model.ContentType
import dev.anaes.qrh.model.contentBlocks
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
        // 11 (version) and 12 (end disclaimer) are drawn by the app's footer, not stored in data
        val unknown = appData.data.flatMap { guideline ->
            guideline.content
                .filter { it.type !in 1..10 && it.type != ContentType.BRANCH }
                .map { "${guideline.code}: type ${it.type}" }
        }
        assertTrue("Unknown content types: $unknown", unknown.isEmpty())
    }

    @Test
    fun `every branch has a header, with its items straight after it`() {
        val problems = appData.data.flatMap { guideline ->
            val content = guideline.content
            val inBlocks = contentBlocks(content).filterIsInstance<ContentBlock.Fork>()
                .flatMap { it.indices }.toSet()
            content.indices.mapNotNull { i ->
                val item = content[i]
                when {
                    item.type == ContentType.BRANCH && item.branch.isEmpty() ->
                        "${guideline.code}[$i]: branch header without an id"
                    item.branch.isNotEmpty() && i !in inBlocks ->
                        "${guideline.code}[$i]: in branch '${item.branch}' but not after its header"
                    else -> null
                }
            }
        }
        assertTrue("Branch problems: $problems", problems.isEmpty())
    }

    @Test
    fun `the forks are in 3-7 and 3-10, two branches each`() {
        val forks = appData.data.associate { guideline ->
            guideline.code to contentBlocks(guideline.content).filterIsInstance<ContentBlock.Fork>()
        }.filterValues { it.isNotEmpty() }

        assertEquals(setOf("3-7", "3-10"), forks.keys)
        assertEquals(listOf("airway", "non-airway"), forks.getValue("3-7").single().branches.map { it.id })
        assertEquals(listOf("arrest", "no-arrest"), forks.getValue("3-10").single().branches.map { it.id })
    }

    @Test
    fun `every guideline has a copyright with a known holder`() {
        val missing = appData.data.filter { guideline ->
            val copyright = guideline.copyright
            copyright == null || copyright.holder !in appData.copyrightHolders
        }.map { it.code }
        assertTrue("Missing or unknown copyright holder: $missing", missing.isEmpty())
    }
}
