package org.helpapaw.helpapaw.authentication.register

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.helpapaw.helpapaw.R

class WhyPhoneDialogFragment: DialogFragment() {

    companion object {
        fun newInstance(): WhyPhoneDialogFragment {
            return WhyPhoneDialogFragment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity!!)
                .setTitle(getString(R.string.txt_why_want_phone))
                .setMessage(getString(R.string.txt_why_phone_description))
                .setPositiveButton(R.string.txt_i_see) {
                    _, _ ->
                }.create()
    }
}