package org.helpapaw.helpapaw.reusable

import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.util.Log

import org.helpapaw.helpapaw.utils.SharingUtils


/**
 * Use throughout the whole app whenever you want to alert the user about something important (e.g. an error)
 */

class AlertDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
        val args = arguments
        val title = args!!.getString(ARG_TITLE, "")
        val message = args.getString(ARG_MESSAGE, "")
        val showSupportButton = args.getBoolean(ARG_SHOW_SUPPORT, false)

        val activity = activity!!

        val alertBuilder = AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
        if (showSupportButton) {
            alertBuilder.setNeutralButton("Contact support") { dialogInterface, i -> SharingUtils.contactSupport(activity) }
        }

        return alertBuilder.create()
    }

    companion object {
        val ARG_TITLE = "AlertDialog.Title"
        val ARG_MESSAGE = "AlertDialog.Message"
        val ARG_SHOW_SUPPORT = "AlertDialog.ShowSupport"

        fun showAlert(title: String, message: String?, showSupportButton: Boolean, fm: FragmentManager?) {
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
}