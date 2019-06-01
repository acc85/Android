package org.helpapaw.helpapaw.viewmodels

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
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
import org.helpapaw.helpapaw.signalsmap.SignalInfoWindowAdapter
import org.helpapaw.helpapaw.signalsmap.SignalsMapFragment
import org.helpapaw.helpapaw.utils.StatusUtils
import org.helpapaw.helpapaw.utils.Utils
import java.io.File
import java.util.ArrayList
import java.util.HashMap


const val PADDING_TOP = 190
const val PADDING_BOTTOM = 160
const val REQUEST_CHECK_SETTINGS = 214

enum class ERROR_TYPE{
    NO_INTERNET
}

enum class MESSAGE_TYPE{
    ADD_SIGNAL
}

sealed class SignalsMapResult {
    data class AnimateAddSignalView(val visibility: Boolean) : SignalsMapResult()
    data class SetThumbnailImage(val uri: String?) : SignalsMapResult()
    data class SetProgressVisibility(val visibility: Boolean) : SignalsMapResult()
    data class ShowMessage(val message: String) : SignalsMapResult()
    data class ShowMessageOfType(val type:MESSAGE_TYPE):SignalsMapResult()
    data class ShowError(val errorType:ERROR_TYPE):SignalsMapResult()
    data class OpenSignalDetailsScreen(val signals:Signal?):SignalsMapResult()
    data class StartResolutionForResult(val requestL:Int):SignalsMapResult()
    data class CheckPermission(val nothing:Unit? = null):SignalsMapResult()

}

class SignalsMapViewModel(

        val imageUtils:ImageUtils

):BaseViewModel() {

    var liveData: MutableLiveData<SignalsMapResult> = MutableLiveData()

    @Bindable
    var addSignalVisible:Int = View.INVISIBLE
        set(value){
            field = value
            notifyChange(BR.addSignalVisible)
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
fun clearSendSignalData(view:SendSignalView, data:String){
    if(data.isEmpty()){
        view.clearData()
    }
}