package com.mttrnd.qrh

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_disclaimers.*
import kotlin.collections.List
import kotlin.system.exitProcess

class Disclaimers : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disclaimers)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        button_exit2.setOnClickListener {
            finishAffinity()
        }

        button_agree_active2.setOnClickListener {
            finish()
        }

    }
}