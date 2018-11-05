package org.helpapaw.helpapaw.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import org.helpapaw.helpapaw.R

class SharingUtils{

    companion object {
        fun contactSupport(context: Context){
            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", context.getString(R.string.string_support_email), null))
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            val chooserIntent:Intent = Intent.createChooser(emailIntent, context.getString(R.string.string_support_email))
            context.startActivity(chooserIntent)
        }
    }
}