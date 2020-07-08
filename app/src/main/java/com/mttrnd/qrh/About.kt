package com.mttrnd.qrh

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class About : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setTitle("About")

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val verCode: Int = BuildConfig.VERSION_CODE
        val verCodeString = verCode.toString()
        val verOutput = "Version $verCodeString"
        findViewById<TextView>(R.id.about_version).setText(verOutput)


    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}