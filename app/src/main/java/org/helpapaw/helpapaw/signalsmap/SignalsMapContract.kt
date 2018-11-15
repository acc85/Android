package org.helpapaw.helpapaw.signalsmap

import org.helpapaw.helpapaw.data.models.Signal

interface SignalsMapContract {

    interface View {

        fun isActive(): Boolean?

        fun showMessage(message: String)

        fun displaySignals(signals: List<Signal>, showPopup: Boolean)

        fun displaySignals(signals: MutableList<Signal>, showPopup: Boolean, focusedSignalId: String)

        fun updateMapCameraPosition(latitude: Double, longitude: Double, zoom: Float?)

        fun setAddSignalViewVisibility(visibility: Boolean)

        fun hideKeyboard()

        fun showSendPhotoBottomSheet()

        fun openCamera()

        fun openGallery()

        fun openLoginScreen()

        fun setThumbnailImage(photoUri: String?)

        fun clearSignalViewData()

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

    interface UserActionsListener {

        fun onInitSignalsMap()

        fun onLocationChanged(latitude: Double, longitude: Double)

        fun onAddSignalClicked(visibility: Boolean)

        fun onCancelAddSignal()

        fun onSendSignalClicked(description: String)

        fun onChoosePhotoIconClicked()

        fun onCameraOptionSelected()

        fun onGalleryOptionSelected()

        fun onSignalPhotoSelected(photoUri: String)

        fun onStoragePermissionForCameraGranted()

        fun onStoragePermissionForGalleryGranted()

        fun onSignalInfoWindowClicked(signal: Signal)

        fun onBackButtonPressed()

        fun onRefreshButtonClicked()

        fun onSignalStatusUpdated(signal: Signal?)

        fun onAuthenticationAction()

        fun onLoginAction()


    }
}