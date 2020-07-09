package com.mttrnd.qrh

import android.animation.LayoutTransition
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_detail_0_4.*
import kotlinx.android.synthetic.main.activity_detail_3_7.*


class Detail : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var linearLayoutManager: LinearLayoutManager

    private fun setupSharedPreferences() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val title = intent.getStringExtra("TITLE")
        val code = intent.getStringExtra("CODE")
        val version = intent.getStringExtra("VERSION")
        setTitle(title)



        when (code) {

            "0-4" -> {
                setContentView(R.layout.activity_detail_0_4)
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                findViewById<TextView>(R.id.detail_version).setText("v.$version").toString()

                val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
                sharedPref.registerOnSharedPreferenceChangeListener(this)

                update_locations()
            }

            "3-7" -> {
                setContentView(R.layout.activity_detail_3_7)
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                findViewById<TextView>(R.id.detail_version).setText("v.$version").toString()

                findViewById<TextView>(R.id.fire_main).text = getString(R.string.fire3)
                findViewById<TextView>(R.id.fire_step).text = "1"

                findViewById<TextView>(R.id.fire_sub).text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(getString(R.string.fire4)).trim()
                } else {
                    Html.fromHtml(getString(R.string.fire4), null, BulletHandler()).trim()
                }

                val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
                sharedPref.registerOnSharedPreferenceChangeListener(this)

                update_firelocations()

                val content = DetailContent.getContentFromFile("3-7.json", this)
                val adapter = CardRecyclerAdapter(content)
                linearLayoutManager = LinearLayoutManager(this)
                detail_recyclerview2.layoutManager = linearLayoutManager
                detail_recyclerview2.setNestedScrollingEnabled(false)
                detail_recyclerview2.adapter = adapter

            }

            else -> {
                setContentView(R.layout.activity_detail)
                val layoutTransition = rootLinearLayout.layoutTransition
                layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
                supportActionBar!!.setDisplayHomeAsUpEnabled(true)

                val filenameSuffix = ".json"
                val filename = code + filenameSuffix

                val content = DetailContent.getContentFromFile(filename, this)
                val adapter = CardRecyclerAdapter(content)

                findViewById<TextView>(R.id.detail_code).setText(code)
                findViewById<TextView>(R.id.detail_version).setText("v.$version").toString()

                linearLayoutManager = LinearLayoutManager(this)
                detail_recyclerview.layoutManager = linearLayoutManager
                detail_recyclerview.setNestedScrollingEnabled(false)
                detail_recyclerview.adapter = adapter
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val code = intent.getStringExtra("CODE")
        when (code) {
            "0-4" -> menuInflater.inflate(R.menu.menu_detail_0_4, menu)
            "3-7" -> menuInflater.inflate(R.menu.menu_detail_0_4, menu)
            else -> menuInflater.inflate(R.menu.menu_detail, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_download -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra("URL"))))
                return true
            }
            R.id.navigation_settings -> {
                this.startActivity(Intent(this,Settings::class.java))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    fun update_locations() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        location_arrest.text = sharedPref.getString("location_arrest", "")
        location_pacing.text = sharedPref.getString("location_pacing", "")
        location_airway.text = sharedPref.getString("location_airway", "")
        location_mh.text = sharedPref.getString("location_mh", "")
        location_la.text = sharedPref.getString("location_la", "")
        location_anaphylaxis.text = sharedPref.getString("location_anaphylaxis", "")
        location_rapid.text = sharedPref.getString("location_rapid", "")
        location_salvage.text = sharedPref.getString("location_salvage", "")
        location_ultrasound.text = sharedPref.getString("location_ultrasound", "")
        location_videoscope.text = sharedPref.getString("location_videoscope", "")
        location_cric.text = sharedPref.getString("location_cric", "")
        location_jet.text = sharedPref.getString("location_jet", "")
        location_scope.text = sharedPref.getString("location_scope", "")
        location_helpp.text = sharedPref.getString("location_helpp", "")
        location_muster.text = sharedPref.getString("location_muster", "")
        location_cooled.text = sharedPref.getString("location_cooled", "")
        location_ice.text = sharedPref.getString("location_ice", "")
        location_sugammadex.text = sharedPref.getString("location_sugammadex", "")

        if(sharedPref.getBoolean("additional_1", false)) {
            additional1.visibility= View.VISIBLE
            additional_1_name.text = sharedPref.getString("additional_1_name", "")
            additional_1_location.text = sharedPref.getString("additional_1_location", "")
        } else {
            additional1.visibility= View.GONE
        }

        if(sharedPref.getBoolean("additional_2", false)) {
            additional2.visibility= View.VISIBLE
            additional_2_name.text = sharedPref.getString("additional_2_name", "")
            additional_2_location.text = sharedPref.getString("additional_2_location", "")
        } else {
            additional2.visibility= View.GONE
        }

        if(sharedPref.getBoolean("additional_3", false)) {
            additional3.visibility= View.VISIBLE
            additional_3_name.text = sharedPref.getString("additional_3_name", "")
            additional_3_location.text = sharedPref.getString("additional_3_location", "")
        } else {
            additional3.visibility= View.GONE
        }

    }

    fun update_firelocations() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        location_firealarm.text = sharedPref.getString("location_firealarm", "")
        location_fireext.text = sharedPref.getString("location_fireext", "")
    }

    override fun onSharedPreferenceChanged(sharedPref: SharedPreferences?, key: String?) {
        val code = intent.getStringExtra("CODE")
        when (code) {
            "0-4" -> update_locations()
            "3-7" -> update_firelocations()
        }


    }
}