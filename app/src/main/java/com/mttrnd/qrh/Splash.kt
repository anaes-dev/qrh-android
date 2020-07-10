package com.mttrnd.qrh

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*
import kotlin.system.exitProcess


class Splash : AppCompatActivity(), ViewTreeObserver.OnScrollChangedListener {

    private val PREF_NAME = "com.mround.bwh.seenwarning"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val seenWarning: Boolean? = sharedPref.getBoolean(PREF_NAME, false)
        val editor = sharedPref.edit()

        if(seenWarning == true) {
            val intent = Intent(this, List::class.java)
            intent.putExtra("STARTUP", true)
            startActivity(intent)
            finish()
        } else {
            setContentView(R.layout.activity_splash)

            val button_exit = findViewById<Button>(R.id.button_exit)
            val button_agree_inactive = findViewById<Button>(R.id.button_agree_inactive)
            val button_agree_active = findViewById<Button>(R.id.button_agree_active)
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
                button_agree_active.isClickable = true
                button_agree_inactive.isClickable = false
            }

            button_exit.setOnClickListener {
                exitProcess(1)
            }

            button_agree_inactive.setOnClickListener {
                Toast.makeText(this@Splash, "Please scroll and agree before continuing.", Toast.LENGTH_SHORT).show()
            }

            button_agree_active.setOnClickListener {
                editor.putBoolean(PREF_NAME, true)
                editor.apply()
                val intent = Intent(this, List::class.java)
                intent.putExtra("FIRSTRUN", true)
                startActivity(intent)
                finish()
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