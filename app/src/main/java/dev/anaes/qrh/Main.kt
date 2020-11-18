package dev.anaes.qrh

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController


class Main : AppCompatActivity() {

    private val navController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


//
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)

        val appBarConfiguration = AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(navController, appBarConfiguration)

    }

    override fun onSupportNavigateUp(): Boolean {
        findNavController(R.id.nav_host_fragment).navigateUp()
        return true
    }

}