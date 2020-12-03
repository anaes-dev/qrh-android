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

    private val vm: MainViewModel by activityViewModels()

    private var _binding: FragmentAboutBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as MainInt).progressShow(false)
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainInt).progressShow(false)
        (activity as MainInt).updateBar(title, "", "", expanded = false, hideKeyboard = true)

        val verCode = BuildConfig.VERSION_NAME
        val verOutput = "Version $verCode"
        binding.qrhHeader.aboutVersion.text = verOutput

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            binding.androidOld.androidOldCard.visibility = View.VISIBLE
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            binding.androidDark.visibility = View.GONE
        }

        binding.btnViewDisclaimers.setOnClickListener {
            findNavController().navigate(R.id.LoadDisclaimers)
        }

        binding.btnViewPrivacy.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://anaes.dev/privacy")))
        }

        binding.qrhInfo.imageViewCC.setOnClickListener{
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.creativecommons.org/licenses/by-nc-sa/4.0/")))
        }


        if (vm.isDarkDisabled) {
            binding.darkDisableSwitch.isChecked = true
        } else if (!vm.isDarkDisabled) {
            binding.darkDisableSwitch.isChecked = false
        }

        binding.darkDisableSwitch.setOnCheckedChangeListener { _, _ ->
            if (binding.darkDisableSwitch.isChecked) {
                vm.isDarkDisabled = true
                (activity as MainInt).setDarkModeDisabled(true)
                (activity as MainInt).recreateActivity()
            } else {
                vm.isDarkDisabled = false
                (activity as MainInt).setDarkModeDisabled(false)
                (activity as MainInt).recreateActivity()

            }

        }

    }

    override fun onResume() {
        super.onResume()
        (activity as MainInt).progressShow(false)
        (activity as MainInt).updateBar(title, "", "", expanded = false, hideKeyboard = true)
    }
}