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

        fun displaySignals(signals: List<Signal>, showPopup: Boolean, focusedSignalId: String)

        fun updateMapCameraPosition(latitude: Double, longitude: Double, zoom: Float?)

        fun setAddSignalViewVisibility(visibility: Boolean)

        fun hideKeyboard()

        fun showSendPhotoBottomSheet()

        fun openCamera()

        fun openGallery()

        fun saveImageFromURI(photoUri: Uri?)

        fun openLoginScreen()

        fun setThumbnailImage(photoUri: String)

        fun clearSignalViewDataView()

        fun setSignalViewProgressVisibility(visibility: Boolean)

        fun openSignalDetailsScreen(signal: Signal)

        fun closeSignalsMapScreen()

        fun showDescriptionErrorMessage()

        fun showAddedSignalMessage()

        fun showNoInternetMessage()

        fun setProgressVisibility(visibility: Boolean)

        fun onLogoutSuccess()
        fun onLogoutFailure(message: String)

    }

}
