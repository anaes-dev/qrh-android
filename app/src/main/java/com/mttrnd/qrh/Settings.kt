package com.mttrnd.qrh

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import java.util.*
import kotlin.collections.ArrayList

class Settings : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val locations = resources.getStringArray(R.array.locations_list)

/*            val locations = listOf(
                "location_arrest",
                "location_pacing",
                "location_airway",
                "location_mh",
                "location_la",
                "location_anaphylaxis",
                "location_rapid",
                "location_salvage",
                "location_ultrasound",
                "location_videoscope",
                "location_cric",
                "location_jet",
                "location_scope",
                "location_helpp",
                "location_muster",
                "location_cooled",
                "location_ice",
                "location_sugammadex",
                "additional_1_name",
                "additional_1_location",
                "additional_2_name",
                "additional_2_location",
                "additional_3_name",
                "additional_3_location"
            )*/

            for(locations in locations) {
                val editTextPreference = preferenceManager.findPreference<EditTextPreference>(locations)
                editTextPreference!!.setOnBindEditTextListener { editText ->
                    editText.inputType = InputType.TYPE_CLASS_TEXT
                    val maxLength = 40
                    val filters = arrayOfNulls<InputFilter>(1)
                    filters[0] = InputFilter.LengthFilter(maxLength)
                    editText.setFilters(filters)
                }
            }

        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}