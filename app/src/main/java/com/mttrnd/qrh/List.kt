package com.mttrnd.qrh

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
    private val PREF_NAME = "com.mround.bwh.seenwarning"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val sharedPref: SharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val seenWarning: Boolean? = sharedPref.getBoolean(PREF_NAME, false)
        val editor = sharedPref.edit()

        if(seenWarning != true){
            editor.putBoolean(PREF_NAME, true)
            editor.apply()

            val newFragment = Firstrun()
            newFragment.show(supportFragmentManager, "")

            val toast = Toast.makeText(applicationContext, "Welcome... disclaimer will show on first run only", Toast.LENGTH_SHORT)
            toast.show()
        }

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
                if(adapter.getItemCount() == 0) {
                    findViewById<TextView>(R.id.list_empty).visibility = View.VISIBLE
                } else {
                    findViewById<TextView>(R.id.list_empty).visibility = View.GONE
                }
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
