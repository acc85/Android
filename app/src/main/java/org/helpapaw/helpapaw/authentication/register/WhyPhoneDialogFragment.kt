package org.helpapaw.helpapaw.authentication.register

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import androidx.appcompat.app.AlertDialog

import org.helpapaw.helpapaw.R

/**
 * Created by iliyan on 7/27/16
 */
class WhyPhoneDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return AlertDialog.Builder(activity)
                .setTitle(getString(R.string.txt_why_want_phone))
                .setMessage(getString(R.string.txt_why_phone_description))
                .setPositiveButton(R.string.txt_i_see
                ) { _, _ -> }.create()
    }

    companion object {

        fun newInstance(): WhyPhoneDialogFragment {
            return WhyPhoneDialogFragment()
        }
    }
}
