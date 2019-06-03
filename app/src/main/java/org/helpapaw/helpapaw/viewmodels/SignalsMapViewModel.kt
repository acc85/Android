package org.helpapaw.helpapaw.viewmodels

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.helpapaw.helpapaw.BR
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.images.ImageUtils
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.repository.*
import org.helpapaw.helpapaw.sendsignal.SendSignalView
import org.helpapaw.helpapaw.signaldetails.SignalDetailsActivity
import org.helpapaw.helpapaw.signalsmap.SignalInfoWindowAdapter
import org.helpapaw.helpapaw.signalsmap.SignalsMapFragment
import org.helpapaw.helpapaw.user.UserManager
import org.helpapaw.helpapaw.utils.StatusUtils
import org.helpapaw.helpapaw.utils.Utils
import java.io.File
import java.util.*


const val PADDING_TOP = 190
const val PADDING_BOTTOM = 160
const val REQUEST_CHECK_SETTINGS = 214

enum class ERROR_TYPE{
    NO_INTERNET, DESCRIPTION
}

enum class MESSAGE_TYPE{
    ADD_SIGNAL
}

abstract class Test(){

}

class Test2:Test(){

}

sealed class SignalsMapResult {
    data class AnimateAddSignalView(val visibility: Boolean) : SignalsMapResult()
    data class SetThumbnailImage(val uri: String?) : SignalsMapResult()
    data class SetProgressVisibility(val visibility: Boolean) : SignalsMapResult()
    data class ShowMessage(val message: String) : SignalsMapResult()
    data class ShowMessageOfType(val type:MESSAGE_TYPE):SignalsMapResult()
    data class ShowError(val errorType:ERROR_TYPE):SignalsMapResult()
    data class ShowSignalMarkerInfo(val marker: Marker):SignalsMapResult()
    data class OpenSignalDetailsScreen(val signals:Signal?):SignalsMapResult()
    data class StartResolutionForResult(val requestL:Int):SignalsMapResult()
    abstract class CheckPermission:SignalsMapResult()
    data class ShowProgress(val showProgress:Boolean):SignalsMapResult()
    object OpenLoginScreen : SignalsMapResult()
    object HideKeyboard : SignalsMapResult()

}

class SignalsMapViewModel(
        val photoRepository:PhotoRepository,
        val pushNotificationsRepository: PushNotificationsRepository,
        val settingsRepository: ISettingsRepository,
        val signalRepository:SignalRepository,
        val userManager: UserManager,
        val imageUtils:ImageUtils,
        val utils: Utils,
        val fusedLocationProviderClient:FusedLocationProviderClient,
        val locationRequest: LocationRequest

):BaseViewModel() {

    var signalsGoogleMap: GoogleMap? = null

    var mCurrentlyShownInfoWindowSignal: Signal? = null

    var mFocusedSignalId: String? = null

    val mDisplayedSignals = ArrayList<Signal>()
    val mSignalMarkers = HashMap<String, Signal>()
    var signalsList: MutableList<Signal>? = ArrayList()

    var mCurrentLat: Double = 0.toDouble()
    var mCurrentLong: Double = 0.toDouble()
    var mZoom: Float = 0.toFloat()

    var currentMapLatitude: Double = 0.toDouble()
    var currentMapLongitude: Double = 0.toDouble()

    var latitude: Double = 0.toDouble()
    var longitude: Double = 0.toDouble()
    var radius: Int = 0
    var timeout: Int = 0

    val locationCallback:LocationCallback = object:LocationCallback(){
        override fun onLocationResult(location: LocationResult?) {
            location?.let{locResult->
                handleNewLocation(locResult.lastLocation)
            }
        }
    }


    var liveData: MutableLiveData<SignalsMapResult> = MutableLiveData()


    @Bindable
    var description:String = ""

    @Bindable
    var addSignalVisible:Int = View.INVISIBLE
        set(value){
            field = value
            notifyChange(BR.addSignalVisible)
        }

    @Bindable
    var clearData:Boolean = false
        set(_){
            photoUri = ""
            field = !field
            notifyChange(BR.clearData)
        }

    @Bindable
    var photoUri:String = ""
        set(value){
            field = value
            sendSignalBitmap = imageUtils.getRotatedBitmap(File(photoUri))
        }

    @Bindable
    var sendSignalBitmap: Bitmap? = null
        set(value){
            field = value
            notifyChange(BR.sendSignalBitmap)
        }

    @Bindable
    var sendSignalViewProgressVisibility:Boolean = false
        set(_){
            field = !field
            notifyChange(BR.sendSignalViewProgressVisibility)
        }

    fun fabOnClick(view:View){
        addSignalVisible  = if(addSignalVisible == View.VISIBLE){ View.INVISIBLE} else { View.VISIBLE }
    }

    @SuppressLint("MissingPermission")
    fun setLastLocation() {
        if (addSignalVisible == View.INVISIBLE) {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task->
                task.result?.let {location->
                    handleNewLocation(location)
                }?: fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            }
        }
    }


    fun sendClick(){
        liveData.value = SignalsMapResult.HideKeyboard
        isLoggedIn()
    }

    fun isLoggedIn(){
        sendSignalViewProgressVisibility = true
        userManager.isLoggedIn(object : UserManager.LoginCallback {
            override fun onLoginSuccess() {
                if (description.isEmpty()) {
                    liveData.value = SignalsMapResult.ShowError(ERROR_TYPE.DESCRIPTION)
                    sendSignalViewProgressVisibility = false
                } else {
                    signalRepository.saveSignal(Signal(description,  Date(), 0, latitude,longitude), object : SignalRepository.SaveSignalCallback {
                        override fun onSignalSaved(signal: Signal) {
                            if (photoUri.isNotEmpty()) {
                                photoRepository.savePhoto(photoUri, signal.id, object : PhotoRepository.SavePhotoCallback {
                                    override fun onPhotoSaved() {
                                        signalsList!!.add(signal)
                                        mFocusedSignalId = signal.id
                                        displaySignals(true)
                                        addSignalVisible = View.INVISIBLE
                                        clearData = true
                                        liveData.value = SignalsMapResult.ShowMessageOfType(MESSAGE_TYPE.ADD_SIGNAL)
                                    }

                                    override fun onPhotoFailure(message: String) {
                                        liveData.value = SignalsMapResult.ShowMessage(message)
                                    }
                                })


                            } else {
                                signalsList!!.add(signal)
                                mFocusedSignalId = signal.id
                                displaySignals(true)
                                addSignalVisible = View.INVISIBLE
                                clearData = true
                            }
                        }

                        override fun onSignalFailure(message: String) {
                            liveData.value = SignalsMapResult.ShowMessage(message)
                        }
                    })

                }
            }

            override fun onLoginFailure(message: String?) {
                sendSignalViewProgressVisibility = false
                liveData.value = SignalsMapResult.OpenLoginScreen
            }
        })

    }

    fun clearLocationData(){
        settingsRepository!!.clearLocationData()
    }


    fun handleNewLocation(location: Location) {
        val longitude = settingsRepository!!.getLastShownLongitude()
        val latitude = settingsRepository!!.getLastShownLatitude()
        val newZoom = settingsRepository!!.getLastShownZoom()

        mCurrentLat = if (latitude == 0.0) location.latitude else latitude
        mCurrentLong = if (longitude == 0.0) location.longitude else longitude
        val zoom = if (newZoom == 0f) calculateMetersToZoom() else newZoom
        updateMapCameraPosition(mCurrentLat, mCurrentLong, zoom)
        //actionsListener
        onLocationChanged(mCurrentLat, mCurrentLong, settingsRepository.getRadius(), settingsRepository.getTimeout())
        pushNotificationsRepository.saveNewDeviceLocation(location)
    }

    private fun calculateMetersToZoom(): Float {
        val radius = (settingsRepository!!.getRadius() * 1000).toDouble()
        val scale = radius / 500
        val zoomLevel = (16 - Math.log(scale) / Math.log(2.0)).toFloat()
        return zoomLevel - 0.5f
    }

    fun onLocationChanged(latitude: Double, longitude: Double, radius: Int, timeout: Int) {
        currentMapLatitude = latitude
        currentMapLongitude = longitude

        if (utils.getDistanceBetween(latitude, longitude, this.latitude, this.longitude) > 300 || this.radius != radius) {
            getAllSignals(latitude, longitude, radius, timeout, false)

            this.latitude = latitude
            this.longitude = longitude
            this.radius = radius
            this.timeout = timeout
        }
    }

    fun getSignals(showPopUp:Boolean){
        getAllSignals(latitude, longitude, radius, timeout, showPopUp)
    }

    private fun getAllSignals(latitude: Double, longitude: Double, radius: Int, timeout: Int, showPopup: Boolean) {
        if (utils.hasNetworkConnection()) {
            liveData.value = SignalsMapResult.ShowProgress(true)

            signalRepository.getAllSignals(latitude, longitude, radius.toDouble(), timeout,
                    object : SignalRepository.LoadSignalsCallback {
                        override fun onSignalsLoaded(signals: MutableList<Signal>?) {
                            signalsList = signals
                            signalRepository.markSignalsAsSeen(signals)
                            displaySignals(showPopup)
                            liveData.value = SignalsMapResult.ShowProgress(false)
                        }

                        override fun onSignalsFailure(message: String) {
                            liveData.value = SignalsMapResult.ShowMessage(message)
                            liveData.value = SignalsMapResult.ShowProgress(false)
                        }
                    })
        } else {
            liveData.value = SignalsMapResult.ShowError(ERROR_TYPE.NO_INTERNET)
        }
    }


    fun displaySignals(showPopup: Boolean) {
        var showPopup = showPopup

        var signal: Signal
        var markerToFocus: Marker? = null
        var signalToFocus: Signal? = null
        var markerToReShow: Marker? = null

        signalsList?.let {signals->
            // Add new signals to the currently displayed ones
            for (newSignal in signals) {
                var alreadyPresent: Signal? = null
                for (presentSignal in mDisplayedSignals) {
                    if (newSignal.id == presentSignal.id) {
                        alreadyPresent = presentSignal
                        break
                    }
                }

                if (alreadyPresent != null) {
                    mDisplayedSignals.remove(alreadyPresent)
                }
                mDisplayedSignals.add(newSignal)
            }
        }

        signalsGoogleMap?.let { gm->
            gm.clear()
            gm.setPadding(0, PADDING_TOP, 0, PADDING_BOTTOM)
            for (i in mDisplayedSignals.indices) {
                signal = mDisplayedSignals[i]

                val markerOptions = MarkerOptions()
                        .position(LatLng(signal.latitude, signal.longitude))
                        .title(signal.title)

                markerOptions.icon(BitmapDescriptorFactory.fromResource(StatusUtils.getPinResourceForCode(signal.status)))

                val marker = gm.addMarker(markerOptions)
                mSignalMarkers[marker.id] = signal

                if (mFocusedSignalId != null) {
                    if (signal.id.equals(mFocusedSignalId!!, ignoreCase = true)) {
                        showPopup = true
                        markerToFocus = marker
                        signalToFocus = signal
                        mFocusedSignalId = null
                    }
                }
                // If an info window was open before signals refresh - reopen it
                if (mCurrentlyShownInfoWindowSignal != null) {
                    if (signal.id.equals(mCurrentlyShownInfoWindowSignal!!.id, ignoreCase = true)) {
                        markerToReShow = marker
                    }
                }
            }

            if (showPopup && markerToFocus != null) {
                markerToFocus?.showInfoWindow()
                updateMapCameraPosition(signalToFocus!!.latitude, signalToFocus!!.longitude, null)
            } else markerToReShow?.showInfoWindow()
        }
    }


    fun calculateZoomToMeters(): Int {
        val visibleRegion = signalsGoogleMap!!.projection.visibleRegion
        val distanceWidth = FloatArray(1)
        val distanceHeight = FloatArray(1)

        val farRight = visibleRegion.farRight
        val farLeft = visibleRegion.farLeft
        val nearRight = visibleRegion.nearRight
        val nearLeft = visibleRegion.nearLeft

        //calculate the distance width (left <-> right of map on screen)
        Location.distanceBetween(
                (farLeft.latitude + nearLeft.latitude) / 2,
                farLeft.longitude,
                (farRight.latitude + nearRight.latitude) / 2,
                farRight.longitude,
                distanceWidth)

        //calculate the distance height (top <-> bottom of map on screen)
        Location.distanceBetween(
                farRight.latitude,
                (farRight.longitude + farLeft.longitude) / 2,
                nearRight.latitude,
                (nearRight.longitude + nearLeft.longitude) / 2,
                distanceHeight)

        //visible radius is (smaller distance) / 2:
        val radius = if (distanceWidth[0] < distanceHeight[0]) distanceWidth[0] / 2 else distanceHeight[0] / 2
        return radius.toInt()
    }



    fun updateMapCameraPosition(latitude: Double, longitude: Double, zoom: Float?) {
        val latLng = LatLng(latitude, longitude)
        val cameraUpdate: CameraUpdate

        if (zoom != null) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom)
        } else {
            cameraUpdate = CameraUpdateFactory.newLatLng(latLng)
        }
        signalsGoogleMap!!.animateCamera(cameraUpdate)
    }

    fun setGoogleMapAsync(googleMap: GoogleMap,layoutInflator:LayoutInflater){
        signalsGoogleMap = googleMap
        //actionsListener
        if (signalsList != null && signalsList!!.size > 0) {
            displaySignals(false)
        }

        signalsGoogleMap!!.setPadding(0, PADDING_TOP, 0, PADDING_BOTTOM)
        signalsGoogleMap!!.setOnMapClickListener{
            mCurrentlyShownInfoWindowSignal = null
        }
        signalsGoogleMap!!.setOnMarkerClickListener{marker->
            mCurrentlyShownInfoWindowSignal = mSignalMarkers[marker.id]
            false
        }
        signalsGoogleMap!!.setOnCameraIdleListener{
            val cameraPosition = signalsGoogleMap!!.cameraPosition
            val cameraTarget = cameraPosition.target
            mCurrentLong = cameraTarget.longitude
            mCurrentLat = cameraTarget.latitude
            mZoom = cameraPosition.zoom
            val radius = calculateZoomToMeters()
            //actionsListener
            onLocationChanged(cameraTarget.latitude, cameraTarget.longitude, radius, settingsRepository.getTimeout())
        }

        signalsGoogleMap!!.setInfoWindowAdapter(SignalInfoWindowAdapter(mSignalMarkers, layoutInflator, photoRepository))
        //actionsListener
        signalsGoogleMap!!.setOnInfoWindowClickListener { marker ->
            liveData.value = SignalsMapResult.ShowSignalMarkerInfo(marker)

            settingsRepository.setLastShownLatitude(mCurrentLat)
            settingsRepository.setLastShownLongitude(mCurrentLong)
            settingsRepository.setLastShownZoom(mZoom)
        }
    }

//    @Bindable
//    var addSignalPinVisible:Int = View.INVISIBLE
//        set(value){
//            field = value
//            notifyChange(BR.addSignalPinVisible)
//        }
//
//    @Bindable
//    var addSignalViewVisible:Int = View.INVISIBLE
//        set(value){
//            field = value
//            notifyChange(BR.addSignalViewVisible)
//        }


}

@BindingAdapter("sendSignalViewProgressVisibility")
fun setSendSignalViewProgressVisibility(view:SendSignalView,visibility:Boolean){
    view.setProgressVisibility(visibility)
}

@BindingAdapter("addSignalPinVisibility")
fun setAddSignalPinVisibility(view:AppCompatImageView, visibility:Int){
    if(visibility == View.VISIBLE){
        view.visibility = View.VISIBLE
        view.alpha = 0.0f
        view.animate()
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(200)
                .translationY(0f)
                .alpha(1.0f)
    }else{
        view.animate()
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(200)
                .alpha(0.0f)
                .withEndAction { view.visibility = View.INVISIBLE }
    }
}

@BindingAdapter("addSignalViewVisibility")
fun setAddSignalViewVisibility(view:SendSignalView, visibility:Int){
    if(visibility == View.VISIBLE){
        view.visibility = View.VISIBLE
        view.alpha = 0.0f
        view.animate()
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(300)
                .translationY(view.height.toFloat())
                .alpha(1.0f)
    }else{
        view.animate()
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setDuration(300)
                .translationY(-view.height.toFloat())
                .withEndAction { view.visibility = View.INVISIBLE }
    }
}

@BindingAdapter("bitmap")
fun setRoundedBitmap(view:SendSignalView, bitmap:Bitmap?) {
    bitmap?.let{
        val drawable = RoundedBitmapDrawableFactory.create(view.resources, bitmap)
        drawable.cornerRadius = 10f
        view.setSignalPhoto(drawable)
    }
}

@BindingAdapter("clearData")
fun clearSendSignalData(view:SendSignalView,@Suppress("UNUSED_PARAMETER") clearData:Boolean){
    view.clearData()
}

@BindingAdapter("mapViewAsync")
fun setMapViewAsync(view:MapView, viewModel:SignalsMapViewModel){
    view.getMapAsync{ googleMap ->
        viewModel.setGoogleMapAsync(googleMap,LayoutInflater.from(view.context))
    }

}
