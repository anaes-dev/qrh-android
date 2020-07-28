package com.mttrnd.qrh

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.activity_splash.imageViewCC
import kotlin.system.exitProcess


class Splash : AppCompatActivity(), ViewTreeObserver.OnScrollChangedListener {

    private val PREFNAME = "com.mround.bwh.seenwarning"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref: SharedPreferences = getSharedPreferences(PREFNAME, Context.MODE_PRIVATE)
        val seenWarning: Boolean? = sharedPref.getBoolean(PREFNAME, false)
        val editor = sharedPref.edit()

        if(seenWarning == true) {
            val intent = Intent(this, List::class.java)
            intent.putExtra("STARTUP", true)
            startActivity(intent)
            finish()
        } else {
            setContentView(R.layout.activity_splash)

            val buttonExit = findViewById<Button>(R.id.button_exit)
            val buttonAgreeInactive = findViewById<Button>(R.id.button_agree_inactive)
            val buttonAgreeActive = findViewById<Button>(R.id.button_agree_active)
            val scrollViewSplash = findViewById<ScrollView>(R.id.splash_scroll)

            scrollViewSplash.getViewTreeObserver().addOnScrollChangedListener(this)

            val verCode = BuildConfig.VERSION_NAME
            val verOutput = "Version $verCode"
            findViewById<TextView>(R.id.about_version).setText(verOutput)

            val scrollBounds = Rect()
            scrollViewSplash.getHitRect(scrollBounds)
            if (scrolledToBottom.getLocalVisibleRect(scrollBounds)) {
                findViewById<Button>(R.id.button_agree_active).visibility = View.VISIBLE
                findViewById<Button>(R.id.button_agree_inactive).visibility = View.GONE
                buttonAgreeActive.isClickable = true
                buttonAgreeInactive.isClickable = false
            }

            buttonExit.setOnClickListener {
                exitProcess(1)
            }

            buttonAgreeInactive.setOnClickListener {
                Snackbar.make(findViewById(android.R.id.content), "Please scroll and read before continuing.", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(ContextCompat.getColor(this, R.color.snackbarBackground))
                    .setAction("Dismiss") {
                    }
                    .show()
            }

            buttonAgreeActive.setOnClickListener {
                editor.putBoolean(PREFNAME, true)
                editor.apply()
                val intent = Intent(this, List::class.java)
                intent.putExtra("FIRSTRUN", true)
                startActivity(intent)
                finish()
            }

            //CC license link
            imageViewCC.setOnClickListener{
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.creativecommons.org/licenses/by-nc-sa/4.0/")))
            }

        }
    }

    override fun onScrollChanged() {
        val scrollViewSplash: ScrollView = findViewById(R.id.splash_scroll)
        val scrollBounds = Rect()
        scrollViewSplash.getHitRect(scrollBounds)

        if (scrolledToBottom.getLocalVisibleRect(scrollBounds)) {
            findViewById<Button>(R.id.button_agree_active).visibility = View.VISIBLE
            findViewById<Button>(R.id.button_agree_inactive).visibility = View.GONE
            button_agree_active.isClickable = true
            button_agree_inactive.isClickable = false
        }

        if (!scrollViewSplash.canScrollVertically(1)) {
            findViewById<Button>(R.id.button_agree_active).visibility = View.VISIBLE
            findViewById<Button>(R.id.button_agree_inactive).visibility = View.GONE
            button_agree_active.isClickable = true
            button_agree_inactive.isClickable = false
        }
    }
}