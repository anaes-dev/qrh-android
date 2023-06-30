package dev.anaes.qrh.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.anaes.qrh.model.DataModel
import dev.anaes.qrh.model.Guideline
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataStoreTest {
    private val dataStore: DataStore = DataStore(ApplicationProvider.getApplicationContext())

    @Test
    fun returns() {
        val data = dataStore.getData()

    }

    @Test
    fun returnsGuidelines() {
        val data = dataStore.getData()
        assert(data.guidelines.isNotEmpty())
    }

    @Test
    fun dataVersionMatches() {
        val data: DataModel = dataStore.getData()
        assertEquals(data.version, "3.00")
    }
}