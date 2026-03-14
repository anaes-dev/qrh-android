package dev.anaes.qrh

import dev.anaes.qrh.model.AppData
import dev.anaes.qrh.model.ContentItem
import dev.anaes.qrh.model.Guideline
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchLogicTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `AppData deserialization parses version and guidelines`() {
        val jsonString = """
            {
              "version": "3.00",
              "data": [
                {
                  "code": "1-1",
                  "title": "Test Guideline",
                  "version": 2,
                  "url": "https://example.com/test.pdf",
                  "content": [
                    {"type": 1, "head": "Heading", "body": "Body text", "step": ""}
                  ]
                }
              ]
            }
        """.trimIndent()

        val appData = json.decodeFromString<AppData>(jsonString)
        assertEquals("3.00", appData.version)
        assertEquals(1, appData.data.size)
        assertEquals("1-1", appData.data[0].code)
        assertEquals("Test Guideline", appData.data[0].title)
        assertEquals(2, appData.data[0].version)
    }

    @Test
    fun `AppData deserialization handles multiple guidelines`() {
        val jsonString = """
            {
              "version": "2.50",
              "data": [
                {"code": "1-1", "title": "First", "version": 1, "url": "https://example.com/1.pdf", "content": []},
                {"code": "2-1", "title": "Second", "version": 3, "url": "https://example.com/2.pdf", "content": []},
                {"code": "3-1", "title": "Third", "version": 1, "url": "https://example.com/3.pdf", "content": []}
              ]
            }
        """.trimIndent()

        val appData = json.decodeFromString<AppData>(jsonString)
        assertEquals(3, appData.data.size)
        assertEquals("2-1", appData.data[1].code)
    }

    @Test
    fun `ContentItem deserialization uses defaults for optional fields`() {
        val jsonString = """{"type": 3}"""
        val item = json.decodeFromString<ContentItem>(jsonString)
        assertEquals(3, item.type)
        assertEquals("", item.step)
        assertEquals("", item.head)
        assertEquals("", item.body)
    }

    @Test
    fun `ContentItem deserialization parses all fields`() {
        val jsonString = """{"type": 5, "step": "1", "head": "Title", "body": "Content"}"""
        val item = json.decodeFromString<ContentItem>(jsonString)
        assertEquals(5, item.type)
        assertEquals("1", item.step)
        assertEquals("Title", item.head)
        assertEquals("Content", item.body)
    }

    @Test
    fun `Guideline find by code returns correct guideline`() {
        val guidelines = listOf(
            Guideline("Alpha", "1-1", 1, "https://example.com/1.pdf", emptyList()),
            Guideline("Beta", "2-3", 2, "https://example.com/2.pdf", emptyList()),
            Guideline("Gamma", "3-5", 1, "https://example.com/3.pdf", emptyList()),
        )

        val found = guidelines.find { it.code == "2-3" }
        assertNotNull(found)
        assertEquals("Beta", found!!.title)
    }

    @Test
    fun `Guideline find by code returns null for missing code`() {
        val guidelines = listOf(
            Guideline("Alpha", "1-1", 1, "https://example.com/1.pdf", emptyList()),
        )

        val found = guidelines.find { it.code == "9-9" }
        assertNull(found)
    }

    @Test
    fun `AppData deserialization ignores unknown keys`() {
        val jsonString = """
            {
              "version": "1.00",
              "unknownField": true,
              "data": [
                {
                  "code": "1-1",
                  "title": "Test",
                  "version": 1,
                  "url": "https://example.com/1.pdf",
                  "content": [],
                  "extraField": "should be ignored"
                }
              ]
            }
        """.trimIndent()

        val appData = json.decodeFromString<AppData>(jsonString)
        assertEquals("1.00", appData.version)
        assertEquals(1, appData.data.size)
    }

    @Test
    fun `Content types cover all expected values`() {
        val types = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        val items = types.map { ContentItem(type = it) }
        assertEquals(12, items.size)
        items.forEachIndexed { index, item ->
            assertEquals(types[index], item.type)
        }
    }

    @Test
    fun `Guideline version is an integer not a string`() {
        val jsonString = """
            {
              "version": "3.00",
              "data": [{
                "code": "1-1", "title": "Test", "version": 5,
                "url": "https://example.com/1.pdf", "content": []
              }]
            }
        """.trimIndent()
        val appData = json.decodeFromString<AppData>(jsonString)
        assertEquals(5, appData.data[0].version)
    }

    @Test
    fun `Empty data array is valid`() {
        val jsonString = """{"version": "1.00", "data": []}"""
        val appData = json.decodeFromString<AppData>(jsonString)
        assertTrue(appData.data.isEmpty())
    }
}
