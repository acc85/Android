package org.helpapaw.helpapaw.signalsmap

import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.squareup.picasso.Picasso

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.repository.PhotoRepository
import org.helpapaw.helpapaw.databinding.InfoWindowSignalBinding
import org.helpapaw.helpapaw.images.RoundedTransformation

/**
 * Created by iliyan on 8/2/16
 */
class SignalInfoWindowAdapter(
        private val signalMarkers: Map<String, Signal> = emptyMap(),
        inflater: LayoutInflater,
        private val photoRepository: PhotoRepository,
        private val selectedSignal: Signal
) : GoogleMap.InfoWindowAdapter {
    private var lastShownMarker: Marker? = null

    internal var binding: InfoWindowSignalBinding = DataBindingUtil.inflate(inflater, R.layout.info_window_signal, null, false)

    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    override fun getInfoContents(marker: Marker): View {
        val signal = signalMarkers[marker.id]
        signal?.let{sig->
            sig.photoUrl = photoRepository.getPhotoUrl(sig.id)
            binding.signalModel = sig
            binding.executePendingBindings()
            selectedSignal.id = sig.id
            if (lastShownMarker == null || lastShownMarker!!.id != marker.id) {
                lastShownMarker = marker
                marker.showInfoWindow()
            }
        }
        return binding.root
    }
}

@BindingAdapter("setStatus")
fun setStatus(view: TextView, status:Int){
    when (status) {
        0 -> view.text = view.context.getString(R.string.txt_status_help_needed)
        1 ->view.text = view.context.getString(R.string.txt_status_somebody_on_the_way)
        2 -> view.text = view.context.getString(R.string.txt_status_solved)
        else -> view.text = view.context.getString(R.string.txt_status_help_needed)
    }
}

@BindingAdapter("setSignalPhoto")
fun setPhotoSignal(view:ImageView,uri:String){
    Picasso.get().load(uri).resize(200, 200)
            .centerCrop()
            .noFade()
            .placeholder(R.drawable.ic_paw)
            .transform(RoundedTransformation(16, 0))
            .into(view)
}
