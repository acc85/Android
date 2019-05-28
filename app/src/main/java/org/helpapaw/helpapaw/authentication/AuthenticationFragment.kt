package org.helpapaw.helpapaw.authentication

import android.app.AlertDialog
import android.text.Html

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.BaseFragment

abstract class AuthenticationFragment : BaseFragment() {
    protected var ppResponseListener: PrivacyPolicyConfirmationContract.UserResponse? = null

    fun showPrivacyPolicyDialog(privacyPolicy: String) {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(Html.fromHtml(privacyPolicy))
                .setPositiveButton(R.string.accept) { dialogInterface, i -> ppResponseListener!!.onUserAcceptedPrivacyPolicy() }
                .setNegativeButton(R.string.decline) { dialogInterface, i -> ppResponseListener!!.onUserDeclinedPrivacyPolicy() }
                .setCancelable(false)
                .show()
    }
}
