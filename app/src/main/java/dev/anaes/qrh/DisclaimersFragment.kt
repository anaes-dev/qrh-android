package dev.anaes.qrh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_about.about_version
import kotlinx.android.synthetic.main.fragment_about.btn_view_disclaimers
import kotlinx.android.synthetic.main.fragment_about.imageViewCC

class DisclaimersFragment : Fragment() {

    private val title = "Disclaimers"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainInt).progressShow(false)
        return inflater.inflate(R.layout.fragment_disclaimers, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainInt).progressShow(false)
        (activity as MainInt).updateBar(title, "", "", false)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainInt).progressShow(false)
        (activity as MainInt).updateBar(title, "", "", false)
    }


}