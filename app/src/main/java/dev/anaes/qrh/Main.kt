package dev.anaes.qrh

import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
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
import kotlinx.android.synthetic.main.activity_main.*


interface PopDetail {
    fun popToDetail(num: Int)
}

class Main : AppCompatActivity(), PopDetail {

    private val prefName = "dev.anaes.qrh.seenwarning"

    private lateinit var appUpdateManager: AppUpdateManager


    override fun onStart() {
        super.onStart()

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
                        101325
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popCompleteUpdate()
            } else {
                Log.e("TAG", "checkForAppUpdateAvailability: something else")
            }
        }

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
                    else -> {
                        Log.i("TAG", "InstallStateUpdatedListener: state: " + state.installStatus())
                    }
                }
            }
        }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101325) {
            if (resultCode != RESULT_OK) {
                Log.d("Update:", "Update flow failed! Result code: $resultCode")

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref: SharedPreferences = getSharedPreferences(prefName, Context.MODE_PRIVATE)
        seenWarning = sharedPref.getBoolean(prefName, false)

        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val appBar: AppBarLayout = findViewById(R.id.app_bar)
        val toolbarLayout: CollapsingToolbarLayout = findViewById(R.id.toolbar_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)

        setSupportActionBar(toolbar)

        val appBarConfiguration = AppBarConfiguration(navController.graph)

        setupActionBarWithNavController(navController, appBarConfiguration)

        if(seenWarning) {
            navController.navigate(R.id.loadList)
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

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


    private fun popCompleteUpdate() {
        val snackbar = Snackbar.make(
            findViewById(R.id.main_container),
            "Update download complete",
            Snackbar.LENGTH_INDEFINITE
        )
        snackbar.setAction("Install") {
            appUpdateManager.completeUpdate()
        }
        snackbar.show()
    }



    companion object {
        var breadcrumbList: ArrayList<String> = ArrayList()
        var breadcrumbCount: Int = 0
        var breadcrumbIsActive: Boolean = false
        var seenWarning: Boolean = false
    }


}