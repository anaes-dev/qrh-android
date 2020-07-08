package com.mttrnd.qrh

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_list.*


class List : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val guidelineList = Guideline.getGuidelinesFromFile("guidelines.json", this)
        val adapter = ListRecyclerAdapter(guidelineList, { guideline : Guideline -> guidelineClicked(guideline) })

        linearLayoutManager = LinearLayoutManager(this)
        list_recyclerview.layoutManager = linearLayoutManager
        list_recyclerview.adapter = adapter


        list_searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.navigation_about -> {
                this.startActivity(Intent(this,About::class.java))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun guidelineClicked(guideline : Guideline) {
        val showGuidelineIntent = Intent(this, Detail::class.java)
        showGuidelineIntent.putExtra("TITLE", guideline.title)
        showGuidelineIntent.putExtra("CODE", guideline.codedisplay)
        showGuidelineIntent.putExtra("VERSION", guideline.version.toString())
        showGuidelineIntent.putExtra("URL", guideline.url)
        startActivity(showGuidelineIntent)
    }

}
