package org.helpapaw.helpapaw.authentication

import android.content.Context
import android.os.AsyncTask

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.utils.Utils

import java.io.IOException
import java.lang.ref.WeakReference

class PrivacyPolicyConfirmationGetter(asker: PrivacyPolicyConfirmationContract.Obtain, context: Context) : AsyncTask<Void, Void, String>() {

    private val weakAsker: WeakReference<PrivacyPolicyConfirmationContract.Obtain>
    private val weakContext: WeakReference<Context>

    init {
        weakAsker = WeakReference(asker)
        weakContext = WeakReference(context)
    }

    override fun doInBackground(vararg params: Void): String? {

        var str: String? = null
        try {
            val context = weakContext.get()
            if (context != null) {
                str = Utils.getHtml(context.getString(R.string.url_privacy_policy))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return str
    }

    override fun onPostExecute(result: String) {
        if (weakAsker.get() != null) {
            weakAsker.get()!!.onPrivacyPolicyObtained(result)
        }
    }
}