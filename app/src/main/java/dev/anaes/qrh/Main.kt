package dev.anaes.qrh

import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
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
import kotlin.Lazy as Lazy1


interface MainInt {
    fun popToDetail(num: Int)
    fun appBarCode(code: String, version: String)
    fun openURL(url: String)
}

class Main : AppCompatActivity(), MainInt {

    private val prefName = "dev.anaes.qrh.seenwarning"

    private lateinit var appUpdateManager: AppUpdateManager

    private val vm: MainViewModel by viewModels()

    private val nc: NavController by lazy { findNavController(R.id.nav_host_fragment) }

    private val fm by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.childFragmentManager }

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

        setContentView(R.layout.activity_main)


        val sharedPref: SharedPreferences = getSharedPreferences(prefName, Context.MODE_PRIVATE)
            seenWarning = sharedPref.getBoolean(prefName, false)


            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController


            val appBar: AppBarLayout = findViewById(R.id.app_bar)
            val toolbarLayout: CollapsingToolbarLayout = findViewById(R.id.toolbar_layout)
            val toolbar: Toolbar = findViewById(R.id.toolbar)

            setSupportActionBar(toolbar)

            val appBarConfiguration = AppBarConfiguration(navController.graph)

            setupActionBarWithNavController(navController, appBarConfiguration)

            if (seenWarning) {
                navController.navigate(R.id.loadList)
            }

    }


    override fun onSupportNavigateUp(): Boolean {

        if(vm.breadcrumbIsActive) {
            vm.breadcrumbCount--
            if(vm.breadcrumbList.isNotEmpty()) {
                vm.breadcrumbList.removeLast()
            }
        }
        this.findNavController(R.id.nav_host_fragment).navigateUp()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val count = fm?.fragments


        for(fragment in count) {


        }

    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putBundle("nav_state", this.findNavController(R.id.nav_host_fragment).saveState())
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        this.findNavController(R.id.nav_host_fragment).restoreState(savedInstanceState.getBundle("nav_state"))
    }

    override fun popToDetail(num: Int) {
        var x = num
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        while (x > 0) {
            navController.popBackStack()
            vm.breadcrumbCount--
            if(vm.breadcrumbList.isNotEmpty()) {
                vm.breadcrumbList.removeLast()
            }
            x--
        }
    }

    override fun appBarCode(code: String, version: String) {
        findViewById<TextView>(R.id.detail_code).text = code
        findViewById<TextView>(R.id.detail_code_2).text = code
        findViewById<TextView>(R.id.detail_version).text = version
    }

    override fun openURL(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
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
        var seenWarning: Boolean = false
    }


}