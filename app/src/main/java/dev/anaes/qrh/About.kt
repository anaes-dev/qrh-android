package dev.anaes.qrh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*

class About : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Setup view
        setContentView(R.layout.activity_about)
        title = "About"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        //Set version number
        val verCode = BuildConfig.VERSION_NAME
        val verOutput = "Version $verCode"
        findViewById<TextView>(R.id.about_version).text = verOutput

        //Launch Disclaimers activity.
        btn_view_disclaimers.setOnClickListener {
            this.startActivity(Intent(this,Disclaimers::class.java))
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