package org.helpapaw.helpapaw.signalsmap

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.data.models.backendless.repositories.BackendlessPhotoRepository
import org.helpapaw.helpapaw.databinding.InfoWindowSignalBinding
import org.helpapaw.helpapaw.utils.images.RoundedTransformation
import java.lang.Exception
import javax.inject.Inject

class SignalInfoWindowAdapter(private val signalMarkers:Map<String, Signal>, private val inflater:LayoutInflater, private var lastShownMarker: Marker? = null): GoogleMap.InfoWindowAdapter{

    @Inject
    constructor(signalsMapFragment: SignalsMapFragment):this(signalsMapFragment.mSignalMarkers,signalsMapFragment.layoutInflater)

    val binding: InfoWindowSignalBinding = DataBindingUtil.inflate(inflater, R.layout.info_window_signal, null, false)

    @Inject
    lateinit var photoRepository: BackendlessPhotoRepository

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {

        val signal = signalMarkers[marker.id]
        if (signal != null) {
            val photoUrl = photoRepository.getPhotoUrl(signal.id)

            if (lastShownMarker == null || lastShownMarker?.id != marker.id) {
                lastShownMarker = marker

                binding.txtSignalTitle.text = signal.title
                binding.txtSignalStatus.text = getStatusString(signal.status!!)

                Picasso.get().load(photoUrl).resize(200, 200)
                        .centerCrop()
                        .noFade()
                        .placeholder(R.drawable.ic_paw)
                        .transform(RoundedTransformation(16, 0))
                        .into(binding.imgSignalPhoto, MarkerCallback(marker))

            }
        }

        return binding.root
    }

    private inner class MarkerCallback internal constructor(marker: Marker) : Callback {

        override fun onError(e: Exception?) {
            Log.e(javaClass.simpleName, "Error loading thumbnail!:"+e?.message)
        }

        internal var marker: Marker? = null

        init {
            this.marker = marker
        }


        override fun onSuccess() {
            if (marker != null && marker!!.isInfoWindowShown) {
                marker!!.showInfoWindow()
            }
        }
    }

    private fun getStatusString(status: Int): String {
        return when (status) {
            0 -> inflater.context.getString(R.string.txt_status_help_needed)
            1 -> inflater.context.getString(R.string.txt_status_somebody_on_the_way)
            2 -> inflater.context.getString(R.string.txt_status_solved)
            else -> inflater.context.getString(R.string.txt_status_help_needed)
        }
    }

}