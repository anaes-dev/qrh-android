package dev.anaes.qrh

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.android.synthetic.main.fragment_about.about_version
import kotlinx.android.synthetic.main.fragment_about.btn_view_disclaimers
import kotlinx.android.synthetic.main.fragment_about.imageViewCC

class AboutFragment : Fragment() {

    private val vm: MainViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.breadcrumbIsActive = false
        activity?.findViewById<AppBarLayout>(R.id.app_bar)?.setExpanded(false)
        activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title = "About"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val verCode = BuildConfig.VERSION_NAME
        val verOutput = "Version $verCode"
        about_version.text = verOutput

        btn_view_disclaimers.setOnClickListener {
            findNavController().navigate(R.id.LoadDisclaimers)
        }

        imageViewCC.setOnClickListener{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.creativecommons.org/licenses/by-nc-sa/4.0/")))
        }
    }


    override fun onResume() {
        super.onResume()
        vm.breadcrumbIsActive = false
    }


}