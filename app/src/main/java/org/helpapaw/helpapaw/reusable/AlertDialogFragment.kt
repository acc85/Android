package org.helpapaw.helpapaw.reusable

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.helpapaw.helpapaw.utils.SharingUtils

class AlertDialogFragment:DialogFragment(){

    companion object {
        const val ARG_TITLE = "AlertDialog.Title"
        const val ARG_MESSAGE = "AlertDialog.Message"
        const val ARG_SHOW_SUPPORT = "AlertDialog.ShowSupport"
        fun showAlert(title: String, message: String, showSupportButton: Boolean, fm: FragmentManager?) {
            val dialog = AlertDialogFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_MESSAGE, message)
            args.putBoolean(ARG_SHOW_SUPPORT, showSupportButton)
            dialog.arguments = args
            if (fm != null) {
                try {
                    dialog.show(fm, "tag")
                } catch (ex: Exception) {
                    Log.e(AlertDialogFragment::class.java.simpleName, "Could not show alert")
                }
            }
        }
    }




    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val args = arguments
        val title = args!!.getString(ARG_TITLE, "")
        val message = args.getString(ARG_MESSAGE, "")
        val showSupportButton = args.getBoolean(ARG_SHOW_SUPPORT, false)

        val alertBuilder = AlertDialog.Builder(activity!!)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
        if (showSupportButton) {
            alertBuilder.setNeutralButton("Contact support") { _, _ -> SharingUtils.contactSupport(activity!!) }
        }


        return alertBuilder.create()
    }

}