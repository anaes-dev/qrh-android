package dev.anaes.qrh

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dev.anaes.qrh.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private val title = "About"

    //    Binding & View Model
    private val vm: MainViewModel by activityViewModels()
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!


    //    Inflate about fragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity as MainInt).progressShow(false)
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }


    //    Populate view
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainInt).progressShow(false)
        (activity as MainInt).updateBar(title, "", "", expanded = false, hideKeyboard = true, opaque = false)

//    Build string from version number
        val verCode = BuildConfig.VERSION_NAME
        val verOutput = "Version $verCode"
        binding.qrhHeader.aboutVersion.text = verOutput

//    Show warning about older Android version if API level < 24

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            binding.androidOld.androidOldCard.visibility = View.VISIBLE
        }

//    Show dark mode option if API level >= 10

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            binding.androidDark.visibility = View.GONE
        }

//    Navigate to disclaimers page

        binding.btnViewDisclaimers.setOnClickListener {
            findNavController().navigate(R.id.LoadDisclaimers)
        }

        //    Navigate to privacy policy


        binding.btnViewPrivacy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://anaes.dev/privacy")))
        }

        //    Navigate to Creative Commons license page


        binding.qrhInfo.imageViewCC.setOnClickListener{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.creativecommons.org/licenses/by-nc-sa/4.0/")))
        }


        //    Set dark mode switch based on current state

        binding.darkDisableSwitch.isChecked = vm.checkDarkDisabled()

        //    Update settings if switch changed


        binding.darkDisableSwitch.setOnCheckedChangeListener { _, _ ->
            if (binding.darkDisableSwitch.isChecked) {
                vm.darkDisabled(true)
                (activity as MainInt).setDarkModeDisabled(true)
                (activity as MainInt).recreateActivity()
            } else {
                vm.darkDisabled(false)
                (activity as MainInt).setDarkModeDisabled(false)
                (activity as MainInt).recreateActivity()

            }

        }

    }

    override fun onResume() {
        super.onResume()
        (activity as MainInt).progressShow(false)
        (activity as MainInt).updateBar(title, "", "", expanded = false, hideKeyboard = true, opaque = false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}