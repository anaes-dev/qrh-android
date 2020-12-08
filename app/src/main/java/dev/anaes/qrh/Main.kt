package dev.anaes.qrh

import android.animation.AnimatorInflater
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dev.anaes.qrh.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface MainInt {
    fun popToDetail(num: Int)
    fun updateBar(
        title: String,
        code: String,
        version: String,
        expanded: Boolean,
        hideKeyboard: Boolean,
        opaque: Boolean
    )
    fun openURL(url: String)
    fun progressShow(show: Boolean)
    fun setDarkModeDisabled(disabled: Boolean)
    fun recreateActivity()
    fun collapseBar(collapse: Boolean)
    fun swipeDetail(code: String, title: String, url: String, version: String)
}

class Main : AppCompatActivity(), MainInt {

    private lateinit var appUpdateManager: AppUpdateManager

    private val vm: MainViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val oldSharedPref: SharedPreferences = getSharedPreferences(
            "com.mttrnd.qrh.seenwarning",
            Context.MODE_PRIVATE
        )

        val sharedPref: SharedPreferences = getSharedPreferences(
            "dev.anaes.qrh",
            Context.MODE_PRIVATE
        )

        if (sharedPref.getInt("version", 0) < BuildConfig.VERSION_CODE) {
            if (!sharedPref.getBoolean("seen_warning", false)) {
                if (oldSharedPref.getBoolean("com.mttrnd.qrh.seenwarning", false)) {
                    startActivity(Intent(this, FirstRun::class.java).putExtra("isUpdate", true))
                } else {
                    startActivity(Intent(this, FirstRun::class.java).putExtra("isUpdate", false))
                }
            } else {
                startActivity(Intent(this, FirstRun::class.java).putExtra("isUpdate", true))
            }
            finish()
        }

        if (sharedPref.getBoolean("night_disabled", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            vm.darkDisabled(true)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            vm.darkDisabled(false)
        }


        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val toolbar: Toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val appBar = binding.appBar
        val params = appBar.layoutParams as CoordinatorLayout.LayoutParams
        if (params.behavior == null)
            params.behavior = AppBarLayout.Behavior()
        val behaviour = params.behavior as AppBarLayout.Behavior
        behaviour.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
            override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                return false
            }
        })

        if(this.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            vm.isDarkMode = true
        } else {
            vm.isDarkMode = false
            appBar.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(this, R.animator.appbar_elevated)
        }

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


    override fun onSupportNavigateUp(): Boolean {
        if (findNavController(R.id.nav_host_fragment).currentDestination.toString()
                .contains("DetailFragment")
        ) {
            progressShow(true)
        }
        findNavController(R.id.nav_host_fragment).navigateUp()
        return true
    }

    override fun onBackPressed() {
        if (findNavController(R.id.nav_host_fragment).currentDestination.toString()
                .contains("DetailFragment")
        ) {
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

//
//    override fun onSaveInstanceState(savedInstanceState: Bundle) {
//        super.onSaveInstanceState(savedInstanceState)
//        savedInstanceState.putBundle(
//            "nav_state",
//            findNavController(R.id.nav_host_fragment).saveState()
//        )
//    }
//
//    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
//        super.onRestoreInstanceState(savedInstanceState)
//        findNavController(R.id.nav_host_fragment).restoreState(savedInstanceState.getBundle("nav_state"))
//    }


    override fun popToDetail(num: Int) {
        var x = num
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        while (x > 0) {
            navController.popBackStack()
            x--
        }
    }

    override fun collapseBar(collapse: Boolean) {
        binding.appBar.setExpanded(!collapse)
    }

    override fun swipeDetail(code: String, title: String, url: String, version: String) {
        val navController = findNavController(R.id.nav_host_fragment)
        val action = DetailFragmentDirections.LoadNewDetail(code, title, url, version)
        navController.navigateUp()
        navController.navigate(action)
    }

    override fun updateBar(
        title: String,
        code: String,
        version: String,
        expanded: Boolean,
        hideKeyboard: Boolean,
        opaque: Boolean,
    ) {
        binding.toolbarLayout.title = title
        binding.detailCode.text = code
        binding.detailVersion.text = version
        binding.appBar.setExpanded(expanded)

        if(vm.isDarkMode) {
            if (opaque) {
                binding.toolbar.setBackgroundColor(
                    ContextCompat.getColor(
                        this,
                        R.color.colorPrimary
                    )
                )
            } else {
                binding.toolbar.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        if (hideKeyboard && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }
    }

    override fun openURL(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    override fun progressShow(show: Boolean) {
        val indicator = binding.progressCircular
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
            binding.mainContainer,
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
            binding.mainContainer,
            "Update failed, please update via Google Play",
            Snackbar.LENGTH_LONG
        )
        snack.show()
    }

    override fun setDarkModeDisabled(disabled: Boolean) {
        val sharedPref: SharedPreferences = getSharedPreferences(
            "dev.anaes.qrh",
            Context.MODE_PRIVATE
        )

        if (disabled) {
            sharedPref.edit()
                .putBoolean("night_disabled", true)
                .apply()
        } else {
            sharedPref.edit()
                .putBoolean("night_disabled", false)
                .apply()
        }
    }

    override fun recreateActivity() {
        startActivity(Intent(this, LoadingActivity::class.java))
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        lifecycleScope.launch {
            delay(400)
            recreate()
        }
    }
}