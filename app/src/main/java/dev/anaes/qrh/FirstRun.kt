package dev.anaes.qrh

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver.OnScrollChangedListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import dev.anaes.qrh.databinding.ActivityFirstrunBinding
import kotlin.system.exitProcess


class FirstRun : AppCompatActivity(), OnScrollChangedListener {

    private lateinit var binding: ActivityFirstrunBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref: SharedPreferences = getSharedPreferences(
            "dev.anaes.qrh",
            Context.MODE_PRIVATE
        )
        val editor = sharedPref.edit()
        binding = ActivityFirstrunBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.splashScroll.viewTreeObserver.addOnScrollChangedListener(this)

        val verCode = BuildConfig.VERSION_NAME
        val verOutput = "Version $verCode"
        binding.qrhHeader.aboutVersion.text = verOutput

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            binding.androidOld.androidOldCard.visibility = View.VISIBLE
        }

        // Change button configuration on scroll to bottom

        val scrollBounds = Rect()
        binding.splashScroll.getHitRect(scrollBounds)
        if (binding.scrolledToBottom.getLocalVisibleRect(scrollBounds)) {
            binding.buttonAgreeActive.visibility = View.VISIBLE
            binding.buttonAgreeInactive.visibility = View.GONE
            binding.buttonAgreeActive.isClickable = true
            binding.buttonAgreeInactive.isClickable = false
        }

        // Change layout if update rather than fresh install

        if(intent.getBooleanExtra("isUpdate", false)) {
            binding.androidUpdated.androidUpdatedCard.visibility = View.VISIBLE
            binding.androidDisclaimerFirst.visibility = View.GONE
            binding.androidDisclaimerUpdate.visibility = View.VISIBLE
        }

        //

        binding.buttonExit.setOnClickListener {
            exitProcess(1)
        }


        val snack = Snackbar.make(
            view,
            "Please scroll and read first.",
            Snackbar.LENGTH_SHORT
        )

        binding.buttonAgreeInactive.setOnClickListener {
            snack.setBackgroundTint(
                ContextCompat.getColor(
                    this,
                    R.color.snackbarBackground
                )
            )
            snack.setAction("Dismiss") {
                snack.dismiss()
            }
            snack.anchorView = binding.buttonLayout
            snack.show()
        }

        binding.buttonAgreeActive.setOnClickListener {
            editor.putBoolean("seen_warning", true)
            editor.putInt("version", BuildConfig.VERSION_CODE)
            editor.apply()
            startActivity(Intent(this, Main::class.java))
            finish()
        }

        //CC license link
        binding.qrhInfo.imageViewCC.setOnClickListener{
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.creativecommons.org/licenses/by-nc-sa/4.0/")
                )
            )
        }

        binding.splashScroll.viewTreeObserver.addOnScrollChangedListener {
            snack.dismiss()
        }


    }

    override fun onScrollChanged() {
        val scrollBounds = Rect()
        binding.splashScroll.getHitRect(scrollBounds)

        //Two methods to check if scrolled to bottom for redundancy (as one does not work reliably on tablet screen where all visible from start)

        if (binding.scrolledToBottom.getLocalVisibleRect(scrollBounds) || !binding.splashScroll.canScrollVertically(
                1
            )) {
            binding.buttonAgreeActive.visibility = View.VISIBLE
            binding.buttonAgreeInactive.visibility = View.GONE
            binding.buttonAgreeActive.isClickable = true
            binding.buttonAgreeInactive.isClickable = false
        }
    }
}