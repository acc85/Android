package org.helpapaw.helpapaw.signalsmap

import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.data.repositories.BackendlessSignalRepository
import org.helpapaw.helpapaw.data.repositories.PhotoRepository
import org.helpapaw.helpapaw.data.repositories.SignalRepository
import org.helpapaw.helpapaw.data.user.UserManager
import org.helpapaw.helpapaw.utils.Injection
import org.helpapaw.helpapaw.utils.Utils

import java.util.ArrayList
import java.util.Date

/**
 * Created by iliyan on 7/28/16
 */
class SignalsMapPresenter(val view: SignalsMapContract.View, val signalRepository: SignalRepository) : Presenter<SignalsMapContract.View>(view), SignalsMapContract.UserActionsListener {

    private val userManager: UserManager
    private val photoRepository: PhotoRepository


    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    private var radius: Int = 0
    private var timeout: Int = 0

    private var currentMapLatitude: Double = 0.toDouble()
    private var currentMapLongitude: Double = 0.toDouble()

    private var photoUri: String? = null
    private var sendSignalViewVisibility: Boolean = false
    private var signalsList: MutableList<Signal>? = null

    private val isViewAvailable: Boolean
        get() = view != null && view.isActive()

    init {
        userManager = Injection.getUserManagerInstance()
        photoRepository = Injection.getPhotoRepositoryInstance()
        sendSignalViewVisibility = false
        signalsList = ArrayList()
    }

    override fun onInitSignalsMap() {
        view.setAddSignalViewVisibility(sendSignalViewVisibility)
        if (!isEmpty(photoUri)) {
            view.setThumbnailImage(photoUri!!)
        }
        if (signalsList != null && signalsList!!.size > 0) {
            view.displaySignals(signalsList!!, false)
        }
    }

    private fun getAllSignals(latitude: Double, longitude: Double, radius: Int, timeout: Int, showPopup: Boolean) {
        if (Utils.getInstance().hasNetworkConnection()) {
            view.setProgressVisibility(true)

            signalRepository.getAllSignals(latitude, longitude, radius.toDouble(), timeout,
                    object : SignalRepository.LoadSignalsCallback {
                        override fun onSignalsLoaded(signals: MutableList<Signal>?) {
                            if (!isViewAvailable) return
                            signalsList = signals
                            signalRepository.markSignalsAsSeen(signals)
                            view.displaySignals(signals, showPopup)
                            view.setProgressVisibility(false)
                        }

                        override fun onSignalsFailure(message: String) {
                            if (!isViewAvailable) return
                            view.showMessage(message)
                            view.setProgressVisibility(false)
                        }
                    })
        } else {
            view.showNoInternetMessage()
        }
    }

    override fun onLocationChanged(latitude: Double, longitude: Double, radius: Int, timeout: Int) {
        currentMapLatitude = latitude
        currentMapLongitude = longitude

        if (Utils.getInstance().getDistanceBetween(latitude, longitude, this.latitude, this.longitude) > 300 || this.radius != radius) {
            getAllSignals(latitude, longitude, radius, timeout, false)

            this.latitude = latitude
            this.longitude = longitude
            this.radius = radius
            this.timeout = timeout
        }
    }

    override fun onAddSignalClicked(visibility: Boolean) {
        setSendSignalViewVisibility(!visibility)
    }

    override fun onCancelAddSignal() {
        view.displaySignals(signalsList!!, false)
    }

    override fun onSendSignalClicked(description: String) {
        view.hideKeyboard()
        view.setSignalViewProgressVisibility(true)

        userManager.isLoggedIn(object : UserManager.LoginCallback {
            override fun onLoginSuccess() {
                if (!isViewAvailable) return

                if (isEmpty(description)) {
                    view.showDescriptionErrorMessage()
                    view.setSignalViewProgressVisibility(false)
                } else {
                    saveSignal(description, Date(), 0, currentMapLatitude, currentMapLongitude)
                }
            }

            override fun onLoginFailure(message: String?) {
                if (!isViewAvailable) return
                view.setSignalViewProgressVisibility(false)
                view.openLoginScreen()
            }
        })
    }

    private fun saveSignal(description: String, dateSubmitted: Date, status: Int, latitude: Double, longitude: Double) {
        signalRepository.saveSignal(Signal(description, dateSubmitted, status, latitude, longitude), object : SignalRepository.SaveSignalCallback {
            override fun onSignalSaved(signal: Signal) {
                if (!isViewAvailable) return
                if (!isEmpty(photoUri)) {
                    savePhoto(photoUri, signal)
                } else {
                    signalsList!!.add(signal)

                    view.displaySignals(signalsList!!, true, signal.id)
                    setSendSignalViewVisibility(false)
                    clearSignalViewData()
                }
            }

            override fun onSignalFailure(message: String) {
                if (!isViewAvailable) return
                view.showMessage(message)
            }
        })
    }

    private fun savePhoto(photoUri: String?, signal: Signal) {
        photoRepository.savePhoto(photoUri, signal.id, object : PhotoRepository.SavePhotoCallback {
            override fun onPhotoSaved() {
                if (!isViewAvailable) return
                signalsList!!.add(signal)
                view.displaySignals(signalsList!!, true, signal.id)
                setSendSignalViewVisibility(false)
                clearSignalViewData()
                view.showAddedSignalMessage()
            }

            override fun onPhotoFailure(message: String) {
                if (!isViewAvailable) return
                view.showMessage(message)
            }
        })
    }

    override fun onChoosePhotoIconClicked() {
        view.hideKeyboard()
        view.showSendPhotoBottomSheet()
    }

    override fun onCameraOptionSelected() {
        view.openCamera()
    }

    override fun onGalleryOptionSelected() {
        view.openGallery()
    }

    override fun onSignalPhotoSelected(photoUri: String) {
        this.photoUri = photoUri
        view.setThumbnailImage(photoUri)
    }

    override fun onStoragePermissionForCameraGranted() {
        view.openCamera()
    }

    override fun onStoragePermissionForGalleryGranted() {
        view.openGallery()
    }

    override fun onSignalInfoWindowClicked(signal: Signal?) {
        view.openSignalDetailsScreen(signal!!)
    }

    override fun onBackButtonPressed() {
        if (sendSignalViewVisibility) {
            setSendSignalViewVisibility(false)
        } else {
            view.closeSignalsMapScreen()
        }
    }

    override fun onRefreshButtonClicked() {
        getAllSignals(latitude, longitude, radius, timeout, false)
    }

    override fun onSignalStatusUpdated(signal: Signal) {
        if (signal == null) return

        for (i in signalsList!!.indices) {
            val currentSignal = signalsList!![i]
            if (currentSignal.id == signal.id) {
                signalsList!!.removeAt(i)
                signalsList!!.add(signal)
                view.displaySignals(signalsList!!, true)
                break
            }
        }
    }

    override fun onAuthenticationAction() {
        view.hideKeyboard()
        val userToken = userManager.userToken

        if (userToken != null && !userToken.isEmpty()) {
            logoutUser()
        } else {
            view.openLoginScreen()
        }
    }

    private fun logoutUser() {
        if (Utils.getInstance().hasNetworkConnection()) {
            userManager.logout(object : UserManager.LogoutCallback {
                override fun onLogoutSuccess() {
                    view.onLogoutSuccess()
                }

                override fun onLogoutFailure(message: String?) {
                    view.onLogoutFailure(message!!)
                }
            })
        } else {
            view.onLogoutFailure("No connection.")
        }
    }

    override fun onLoginAction() {}

    private fun clearSignalViewData() {
        view.clearSignalViewData()
        photoUri = null
    }

    private fun setSendSignalViewVisibility(visibility: Boolean) {
        sendSignalViewVisibility = visibility
        view.setAddSignalViewVisibility(visibility)
    }

    private fun isEmpty(value: String?): Boolean {
        return !(value != null && value.length > 0)
    }

    companion object {

        private val DEFAULT_MAP_ZOOM = 14.5f
        val DEFAULT_SEARCH_RADIUS = 10
        val DEFAULT_SEARCH_TIMEOUT = 7
        private val DATE_TIME_FORMAT = "MM/dd/yyyy hh:mm:ss"
    }
}