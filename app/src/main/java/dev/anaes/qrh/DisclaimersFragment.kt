package dev.anaes.qrh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_about.about_version
import kotlinx.android.synthetic.main.fragment_about.btn_view_disclaimers
import kotlinx.android.synthetic.main.fragment_about.imageViewCC

class DisclaimersFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Main.breadcrumbIsActive = false
        activity?.progress_circular?.visibility = View.GONE
        activity?.findViewById<AppBarLayout>(R.id.app_bar)?.setExpanded(false)
        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = "Disclaimers"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_disclaimers, container, false)
    }

    override fun onResume() {
        super.onResume()
        Main.breadcrumbIsActive = false
        activity?.progress_circular?.visibility = View.GONE
        activity?.findViewById<AppBarLayout>(R.id.app_bar)?.setExpanded(false)
        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = "Disclaimers"
    }


}