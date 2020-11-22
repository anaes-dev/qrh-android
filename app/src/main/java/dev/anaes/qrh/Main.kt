package dev.anaes.qrh

import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.icu.util.TimeZone
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import java.security.Timestamp

interface MainInt {
    fun popToDetail(num: Int)
    fun updateBar(title: String, code: String, version: String, expanded: Boolean)
    fun openURL(url: String)
    fun progressShow(show: Boolean)
}

class Main : AppCompatActivity(), MainInt {

    private lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref: SharedPreferences = getSharedPreferences("dev.anaes.qrh", Context.MODE_PRIVATE)

        if (!sharedPref.getBoolean("seen_warning", false)) {
            startActivity(Intent(this, FirstRun::class.java))
            finish()
        } else {

            setContentView(R.layout.activity_main)

            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            val appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)

            val currentTime = System.currentTimeMillis()
            val lastCheckedTime = sharedPref.getLong("update_checked", 0)

            if ((currentTime - lastCheckedTime) > 600000) {
                appUpdateManager = AppUpdateManagerFactory.create(this)
                appUpdateManager.registerListener(installStateUpdatedListener)

                appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    ) {
                        try {
                            appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.FLEXIBLE,
                                this,
                                10133
                            )
                        } catch (e: SendIntentException) {
                            e.printStackTrace()
                        }
                    } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        popCompleteUpdate()
                    }
                }
                sharedPref.edit()
                    .putLong("update_checked", System.currentTimeMillis())
                    .apply()
            }

        }
    }


    override fun onSupportNavigateUp(): Boolean {
        if(findNavController(R.id.nav_host_fragment).currentDestination.toString().contains("DetailFragment")) {
            progressShow(true)
        }
        findNavController(R.id.nav_host_fragment).navigateUp()
        return true
    }

    override fun onBackPressed() {
        if(findNavController(R.id.nav_host_fragment).currentDestination.toString().contains("DetailFragment")) {
            progressShow(true)
        }
        super.onBackPressed()
    }


    private var installStateUpdatedListener: InstallStateUpdatedListener =
        object : InstallStateUpdatedListener {
            override fun onStateUpdate(state: InstallState) {
                when {
                    state.installStatus() == InstallStatus.DOWNLOADED -> {
                        popCompleteUpdate()
                    }
                    state.installStatus() == InstallStatus.INSTALLED -> {
                        appUpdateManager.unregisterListener(this)
                    }
                }
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10133) {
            if (resultCode != RESULT_OK) {
                popFailedUpdate()
            }
        }
    }


    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putBundle("nav_state", findNavController(R.id.nav_host_fragment).saveState())
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        findNavController(R.id.nav_host_fragment).restoreState(savedInstanceState.getBundle("nav_state"))
    }



    override fun popToDetail(num: Int) {
        var x = num
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        while (x > 0) {
            navController.popBackStack()
            x--
        }
    }

    override fun updateBar(title: String, code: String, version: String, expanded: Boolean) {
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title
        findViewById<TextView>(R.id.detail_code).text = code
        findViewById<TextView>(R.id.detail_version).text = version
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(expanded)
    }

    override fun openURL(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun progressShow(show: Boolean) {
        val indicator = findViewById<ProgressBar>(R.id.progress_circular)
        if (show) {
            indicator.visibility = View.VISIBLE
        } else {
            indicator.animate()
                .setDuration(100)
                .alpha(0F)
                .withEndAction {
                    indicator.visibility = View.GONE
                    indicator.alpha = 0.5F
                }
        }
    }

    private fun popCompleteUpdate() {
        val snack = Snackbar.make(
            findViewById(R.id.main_container),
            "Update ready to install",
            Snackbar.LENGTH_INDEFINITE
        )
        snack.setAction("Install") {
            appUpdateManager.completeUpdate()
        }
        snack.show()
    }

    private fun popFailedUpdate() {
        val snack = Snackbar.make(
            findViewById(R.id.main_container),
            "Update failed, please update via Google Play",
            Snackbar.LENGTH_LONG
        )
        snack.show()
    }

}