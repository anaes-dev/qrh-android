package com.mttrnd.qrh

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

class Firstrun : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(R.string.welcome)
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setMessage(R.string.about_5)
                .setPositiveButton(R.string.close,
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel();
                    })
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}