package org.helpapaw.helpapaw.signalsmap

import android.net.Uri
import org.helpapaw.helpapaw.models.Signal

/**
 * Created by iliyan on 7/28/16
 */
interface SignalsMapContract {

    interface View {

        fun showMessage(message: String)

        fun hideKeyboard()

        fun showSendPhotoBottomSheet()

        fun openCamera()

        fun openGallery()

        fun openLoginScreen()

        fun closeSignalsMapScreen()

        fun setProgressVisibility(visibility: Boolean)

    }

}
