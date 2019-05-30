package org.helpapaw.helpapaw.viewmodels

import android.view.View
import androidx.databinding.Bindable
import org.helpapaw.helpapaw.BuildConfig
import org.helpapaw.helpapaw.utils.SharingUtils

class AboutViewModel : BaseViewModel() {

    @Bindable
    var aboutText = BuildConfig.VERSION_NAME

    fun contactSupport(view: View){
        SharingUtils.contactSupport(view.context)
    }
}
