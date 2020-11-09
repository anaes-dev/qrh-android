package com.mttrnd.qrh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_newpackage.*


class NewPackage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setup view
        setContentView(R.layout.activity_newpackage)

        //CC license link
        GooglePlay.setOnClickListener{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=dev.anaes.qrh")))
        }

        buttonOld.setOnClickListener {
            this.startActivity(Intent(this,Splash::class.java))
            finish()
        }

    }

    //Close activity on back / up navigation
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}