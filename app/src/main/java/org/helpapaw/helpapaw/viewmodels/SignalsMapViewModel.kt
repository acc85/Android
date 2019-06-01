package org.helpapaw.helpapaw.viewmodels

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
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
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.repository.*
import org.helpapaw.helpapaw.sendsignal.SendSignalView
import org.helpapaw.helpapaw.signalsmap.SignalInfoWindowAdapter
import org.helpapaw.helpapaw.signalsmap.SignalsMapFragment
import org.helpapaw.helpapaw.utils.StatusUtils
import org.helpapaw.helpapaw.utils.Utils
import java.util.ArrayList
import java.util.HashMap


const val PADDING_TOP = 190
const val PADDING_BOTTOM = 160
const val REQUEST_CHECK_SETTINGS = 214

enum class ERROR_TYPE{
    NO_INTERNET
}


sealed class SignalsMapResult {
    data class AnimateAddSignalView(val visibility: Boolean) : SignalsMapResult()
    data class SetThumbnailImage(val uri: String?) : SignalsMapResult()
    data class SetProgressVisibility(val visibility: Boolean) : SignalsMapResult()
    data class ShowMessage(val message: String) : SignalsMapResult()
    data class ShowError(val errorType:ERROR_TYPE):SignalsMapResult()
    data class OpenSignalDetailsScreen(val signals:Signal?):SignalsMapResult()
    data class StartResolutionForResult(val requestL:Int):SignalsMapResult()
    data class CheckPermission(val nothing:Unit? = null):SignalsMapResult()

}

class SignalsMapViewModel(
):BaseViewModel() {

    var liveData: MutableLiveData<SignalsMapResult> = MutableLiveData()


}