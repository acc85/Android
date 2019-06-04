package org.helpapaw.helpapaw.signalsmap

import androidx.databinding.DataBindingUtil
import android.util.Log
import android.view.LayoutInflater
import android.view.View

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.repository.PhotoRepository
import org.helpapaw.helpapaw.databinding.InfoWindowSignalBinding
import org.helpapaw.helpapaw.images.RoundedTransformation
import java.lang.Exception

/**
 * Created by iliyan on 8/2/16
 */
class SignalInfoWindowAdapter(
        private val signalMarkers: Map<String, Signal> = emptyMap(),
        private val inflater: LayoutInflater,
        private val photoRepository: PhotoRepository,
        private val selectedSignal: Signal
) : GoogleMap.InfoWindowAdapter {
    private var lastShownMarker: Marker? = null

    internal var binding: InfoWindowSignalBinding

    init {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.info_window_signal, null, false)
    }

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {

        val signal = signalMarkers[marker.id]
        selectedSignal.id = signal!!.id
        if (signal != null) {
            val photoUrl = photoRepository.getPhotoUrl(signal.id)

            if (lastShownMarker == null || lastShownMarker!!.id != marker.id) {
                lastShownMarker = marker

                binding.txtSignalTitle.text = signal.title
                binding.txtSignalStatus.text = getStatusString(signal.status)

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
        internal var marker: Marker? = null

        init {
            this.marker = marker
        }

        override fun onError(e: Exception?) {
            Log.e(javaClass.simpleName, "Error loading thumbnail!")
        }

        override fun onSuccess() {
            if (marker != null && marker!!.isInfoWindowShown) {
                marker!!.showInfoWindow()
            }
        }
    }

    private fun getStatusString(status: Int): String {
        when (status) {
            0 -> return inflater.context.getString(R.string.txt_status_help_needed)
            1 -> return inflater.context.getString(R.string.txt_status_somebody_on_the_way)
            2 -> return inflater.context.getString(R.string.txt_status_solved)
            else -> return inflater.context.getString(R.string.txt_status_help_needed)
        }
    }
}
