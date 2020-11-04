package dev.anaes.qrh

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_list.*


class List : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        // Get flags from splash activity

        val isStartup = intent.getBooleanExtra("STARTUP", false)
        val isFirstrun = intent.getBooleanExtra("FIRSTRUN", false)
        val rvScrollListener = findViewById<RecyclerView>(R.id.list_recyclerview)


        // Check if fresh startup

        if(isStartup) {
            val snack =
                Snackbar
                    .make(container, "Open source adaptation of Quick Reference Handbook\nNo guarantees of completeness, accuracy or performance\nShould not override your own knowledge and judgement", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(this, R.color.snackbarBackground))
                    .setDuration(8000)
                    .setAction(R.string.title_about) {
                        this.startActivity(Intent(this,About::class.java))
                    }
            val snackbarView: View = snack.view
            val snackTextView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
            snackTextView.maxLines = 3
            snackTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12F)
            snack.show()


            // Dismiss snackbar if list scrolled (otherwise will stay visible for 8 seconds)

            rvScrollListener.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 2) snack.dismiss()
                }
            })
        }

        // Check if first run

        if(isFirstrun) {
            val snack =
                Snackbar
                    .make(container, "Welcome to the Quick Reference Handbook", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getColor(this, R.color.snackbarBackground))
            snack.show()
        }

        // Pull guideline list from file and display in recyclerview

        val guidelineList =
            Guideline.getGuidelinesFromFile(
                "guidelines.json",
                this
            )
        val adapter =
            ListRecyclerAdapter(guidelineList) { guideline: Guideline ->
                guidelineClicked(guideline)
            }

        linearLayoutManager = LinearLayoutManager(this)
        list_recyclerview.layoutManager = linearLayoutManager
        list_recyclerview.adapter = adapter

        // Search listener (+ adjust visibility of empty list message)

        list_searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                if(adapter.itemCount == 0) {
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

    // Pass details of clicked guideline in activity intent

    private fun guidelineClicked(guideline : Guideline) {
        val showGuidelineIntent = Intent(this, Detail::class.java)
        showGuidelineIntent.putExtra("TITLE", guideline.title)
        showGuidelineIntent.putExtra("CODE", guideline.code)
        showGuidelineIntent.putExtra("VERSION", guideline.version.toString())
        showGuidelineIntent.putExtra("URL", guideline.url)
        startActivity(showGuidelineIntent)
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }



}
