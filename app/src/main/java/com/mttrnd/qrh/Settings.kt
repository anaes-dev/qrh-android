package com.mttrnd.qrh

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

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

            for(locations in locations) {
                val editTextPreference = preferenceManager.findPreference<EditTextPreference>(locations)
                editTextPreference!!.setOnBindEditTextListener { editText ->
                    editText.inputType = InputType.TYPE_CLASS_TEXT
                    val maxLength = 50
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