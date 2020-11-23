package dev.anaes.qrh

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_about.*
import kotlinx.android.synthetic.main.qrh_info.*

class AboutFragment : Fragment() {

    private val title = "About"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainInt).progressShow(false)
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainInt).progressShow(false)
        (activity as MainInt).updateBar(title, "", "", expanded = false, hideKeyboard = true)

        val verCode = BuildConfig.VERSION_NAME
        val verOutput = "Version $verCode"
        view.findViewById<TextView>(R.id.about_version).text = verOutput

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            android_old.visibility = View.VISIBLE
        }

        btn_view_disclaimers.setOnClickListener {
            findNavController().navigate(R.id.LoadDisclaimers)
        }


        btn_view_privacy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://anaes.dev/privacy")))
        }

        imageViewCC.setOnClickListener{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.creativecommons.org/licenses/by-nc-sa/4.0/")))
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainInt).progressShow(false)
        (activity as MainInt).updateBar(title, "", "", expanded = false, hideKeyboard = true)
    }
}