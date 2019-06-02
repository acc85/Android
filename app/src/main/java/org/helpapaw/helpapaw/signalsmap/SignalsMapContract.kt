package org.helpapaw.helpapaw.signalsmap

import android.net.Uri
import org.helpapaw.helpapaw.models.Signal

/**
 * Created by iliyan on 7/28/16
 */
interface SignalsMapContract {

    interface View {

        fun isActive(): Boolean

        fun showMessage(message: String)

        fun displaySignals(signals: List<Signal>?, showPopup: Boolean)

        fun updateMapCameraPosition(latitude: Double, longitude: Double, zoom: Float?)

        fun hideKeyboard()

        fun showSendPhotoBottomSheet()

        fun openCamera()

        fun openGallery()

        fun saveImageFromURI(photoUri: Uri?)

        fun openLoginScreen()

        fun closeSignalsMapScreen()

        fun showDescriptionErrorMessage()

        fun showAddedSignalMessage()

        fun showNoInternetMessage()

        fun setProgressVisibility(visibility: Boolean)

    }

}
