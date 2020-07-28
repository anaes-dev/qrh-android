package com.mttrnd.qrh

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_about.*

class About : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setup view
        setContentView(R.layout.activity_about)
        setTitle("About")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Set version number
        val verCode = BuildConfig.VERSION_NAME
        val verOutput = "Version $verCode"
        findViewById<TextView>(R.id.about_version).setText(verOutput)

        //Launch Disclaimers activity.
        btn_view_disclaimers.setOnClickListener {
            this.startActivity(Intent(this,Disclaimers::class.java))
        }

        //Reset DefaultSharedPreferences, SharedPreferences, and exit.
        btn_resetexit.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply()
            val sharedPref: SharedPreferences = getSharedPreferences("com.mround.bwh.seenwarning", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.clear()
            editor.commit()
            finishAffinity()
        }

        //CC license link
        imageViewCC.setOnClickListener{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.creativecommons.org/licenses/by-nc-sa/4.0/")))
        }

    }

    //Close activity on back / up navigation
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}