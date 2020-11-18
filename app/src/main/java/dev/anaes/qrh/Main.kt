package dev.anaes.qrh

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout

interface PopDetail {
    fun popToDetail(num: Int)
}

class Main : AppCompatActivity(), PopDetail {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBar: AppBarLayout = findViewById(R.id.app_bar)
        val toolbarLayout: CollapsingToolbarLayout = findViewById(R.id.toolbar_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        val appBarConfiguration = AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(navController, appBarConfiguration)

    }

    @SuppressLint("RestrictedApi")
    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        var count = navController.backStack.count()

        if(breadcrumbIsActive) {
            breadcrumbCount--
            breadcrumbList.removeLast()
        }

        findNavController(R.id.nav_host_fragment).navigateUp()
        return true
    }

    override fun popToDetail(num: Int) {
        var x = num
        Log.d("test", num.toString())
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        while (x > 0) {
            navController.popBackStack()
            breadcrumbCount--
            breadcrumbList.removeLast()
            x--
        }
    }

    companion object {
        var breadcrumbList: ArrayList<String> = ArrayList()
        var breadcrumbCount: Int = 0
        var breadcrumbIsActive: Boolean = false
    }


}