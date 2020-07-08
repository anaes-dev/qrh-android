package com.mttrnd.qrh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_detail.*

class Detail : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        val title = intent.getStringExtra("TITLE")
        val code = intent.getStringExtra("CODE")
        val version = intent.getStringExtra("VERSION")

        setTitle(title)

        val filenameSuffix = ".json"
        val filename = code + filenameSuffix

        val content = DetailContent.getContentFromFile(filename, this)
        val adapter = CardRecyclerAdapter(content)

        findViewById<TextView>(R.id.detail_version).setText("v.$version").toString()

        linearLayoutManager = LinearLayoutManager(this)
        detail_recyclerview.layoutManager = linearLayoutManager
        detail_recyclerview.setNestedScrollingEnabled(false);
        detail_recyclerview.adapter = adapter

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_download -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(intent.getStringExtra("URL"))))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}