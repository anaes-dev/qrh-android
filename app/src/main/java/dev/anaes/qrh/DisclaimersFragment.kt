package dev.anaes.qrh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

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
        (activity as MainInt).updateBar("QRH", "", "", expanded = false, hideKeyboard = true)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainInt).progressShow(false)
        (activity as MainInt).updateBar("QRH", "", "", expanded = false, hideKeyboard = true)
    }


}