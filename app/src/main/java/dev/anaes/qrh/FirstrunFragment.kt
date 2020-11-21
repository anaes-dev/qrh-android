package dev.anaes.qrh

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_firstrun.*
import kotlin.system.exitProcess

class FirstrunFragment : Fragment(), ViewTreeObserver.OnScrollChangedListener {

    private val prefName = "dev.anaes.qrh.seenwarning"
    private val vm: MainViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.breadcrumbIsActive = false
        activity?.progress_circular?.visibility = View.GONE
        activity?.findViewById<AppBarLayout>(R.id.app_bar)?.setExpanded(false)
        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = "QRH"

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_firstrun, container, false)
    }

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val verCode = BuildConfig.VERSION_NAME
        val verOutput = "Version $verCode"
        about_version.text = verOutput

        splash_scroll.viewTreeObserver.addOnScrollChangedListener(this)

        button_agree_inactive.setOnClickListener {
            Snackbar.make(view, "Please scroll and read before continuing.", Snackbar.LENGTH_SHORT)
                .setAction("Dismiss") {
                }
                .show()
        }

        button_agree_active.setOnClickListener {
            splash_scroll.viewTreeObserver.removeOnScrollChangedListener(this)
            findNavController().navigate(R.id.loadList)
        }

        button_exit.setOnClickListener {
            exitProcess(1)
        }

        imageViewCC.setOnClickListener{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.creativecommons.org/licenses/by-nc-sa/4.0/")))
        }
    }

    override fun onResume() {
        super.onResume()
        vm.breadcrumbIsActive = false
        activity?.progress_circular?.visibility = View.GONE
        activity?.findViewById<AppBarLayout>(R.id.app_bar)?.setExpanded(false)
        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = "QRH"
        splash_scroll.viewTreeObserver.addOnScrollChangedListener(this)
    }

    override fun onScrollChanged() {
        val scrollBounds = Rect()
        splash_contents.getHitRect(scrollBounds)
        if (scrolledToBottom.getLocalVisibleRect(scrollBounds) || !splash_scroll.canScrollVertically(1)) {
            button_agree_active.visibility = View.VISIBLE
            button_agree_inactive.visibility = View.GONE
            button_agree_active.isClickable = true
            button_agree_inactive.isClickable = false
        }
    }

    override fun onPause() {
        super.onPause()
        splash_scroll.viewTreeObserver.removeOnScrollChangedListener(this)
    }

    override fun onStop() {
        super.onStop()
        splash_scroll.viewTreeObserver.removeOnScrollChangedListener(this)
    }


}