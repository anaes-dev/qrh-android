package dev.anaes.qrh

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.activity_detail_scrollingcontent.*


class Detail : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(List.breadcrumbCount > 9) {
            val text = "Too many guidelines open"
            val duration = Toast.LENGTH_LONG
            val toast = Toast.makeText(applicationContext, text, duration)
            toast.show()
            finish()
        }

        val data = intent.data

        val code: String? = data?.toString()?.replace("dev.anaes.qrh.detail://","") ?: intent.getStringExtra("CODE")

        val title: String? = if(data != null) {
            getDetailsFromCode(code)[0]
        } else {
            intent.getStringExtra("TITLE")
        }

        val version: String? = if(data != null) {
            "v."+getDetailsFromCode(code)[2]
        } else {
            "v."+intent.getStringExtra("VERSION")
        }

        setTitle(title)

        //Populate content

        setContentView(R.layout.activity_detail)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        findViewById<TextView>(R.id.detail_code).text = code
        findViewById<TextView>(R.id.detail_code_2).text = code
        findViewById<TextView>(R.id.detail_version).text = version


        List.breadcrumbHash.add(this.hashCode().toString())
        List.breadcrumbList.add(code.toString())
        List.breadcrumbCount++

        val breadcrumbString = StringBuilder()
        for (bc in List.breadcrumbList) {
            breadcrumbString.append(" > ")
            breadcrumbString.append(bc)
        }

        findViewById<TextView>(R.id.detail_breadcrumb).text = breadcrumbString
        findViewById<ConstraintLayout>(R.id.detail_home).setOnClickListener {
            List.breadcrumbCount == 0
            List.breadcrumbList.clear()
            Intent(this, List::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }.also { startActivity(it) }
            finish()
        }

        //Feed relevant JSON to Recyclerview / CardRecyclerAdapter

        val filenameSuffix = ".json"
        val filename = code + filenameSuffix

        val content = DetailContent.getContentFromFile(
            filename,
            this
        )
        val adapter = CardRecyclerAdapter(content, code)

        linearLayoutManager = LinearLayoutManager(this)
        detail_recyclerview.layoutManager = linearLayoutManager
        detail_recyclerview.isNestedScrollingEnabled = false
        detail_recyclerview.adapter = adapter



    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val downloadURL: String? = if(intent.data != null) {
            getDetailsFromCode(intent.data.toString().replace("dev.anaes.qrh.detail://",""))[1]
        } else {
            intent.getStringExtra("URL")
        }

        return when (item.itemId) {
            R.id.navigation_download -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(downloadURL)))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        List.breadcrumbList.removeAt(List.breadcrumbList.lastIndex)
        List.breadcrumbCount--
        finish()
        return true
    }

    fun getDetailsFromCode(codePassed: String?): Array<String> {
        val guideList = Guideline.getGuidelinesFromFile(
            "guidelines.json",
            this
        )
        val position: Int = guideList.indexOfFirst { it.code == codePassed }
        return arrayOf(guideList[position].title,guideList[position].url,guideList[position].version.toString())
    }
}