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

    private companion object {
        // Parsed once for all tests. Unit tests run with the module directory as the working directory.
        val appData: AppData = Json { ignoreUnknownKeys = true }
            .decodeFromString(File("src/main/assets/data.json").readText())
    }

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

    @Test
    fun `tables are only in boxes, with every row as wide as the headings`() {
        val problems = appData.data.flatMap { guideline ->
            guideline.content.mapIndexedNotNull { i, item ->
                val table = item.table ?: return@mapIndexedNotNull null
                when {
                    item.type !in 5..9 -> "${guideline.code}[$i]: table in a type ${item.type} item"
                    table.rows.isEmpty() -> "${guideline.code}[$i]: table without rows"
                    table.rows.any { it.size != table.columns.size } ->
                        "${guideline.code}[$i]: a row's width differs from the headings'"
                    else -> null
                }
            }
        }
        assertTrue("Table problems: $problems", problems.isEmpty())
    }

    @Test
    fun `a table's text fallback has every value in it`() {
        // The body is what search reads and what apps without tables show, so it must not drift
        val missing = appData.data.flatMap { guideline ->
            guideline.content.flatMap { item ->
                val table = item.table ?: return@flatMap emptyList()
                table.rows.flatten().filter { "${it}:" !in item.body && it !in item.body }
                    .map { "${guideline.code}: $it" }
            }
        }
        assertTrue("Missing from the body: $missing", missing.isEmpty())
    }

    @Test
    fun `4-1 has the oxygen cylinder table`() {
        val box = appData.data.single { it.code == "4-1" }.content.single { it.table != null }
        assertEquals("Box B: OXYGEN CYLINDER REMAINING CONTENTS", box.head)
        assertEquals(listOf("C", "D", "E", "F", "G", "J"), box.table!!.rows.map { it.first() })
        assertEquals(listOf("J", "1700 L", "3400 L", "6800 L"), box.table.rows.last())
    }
}
