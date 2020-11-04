package dev.anaes.qrh

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_disclaimers.*

class Disclaimers : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disclaimers)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        // Button listeners

        button_exit2.setOnClickListener {
            finishAffinity()
        }

        button_agree_active2.setOnClickListener {
            finish()
        }

    }
}