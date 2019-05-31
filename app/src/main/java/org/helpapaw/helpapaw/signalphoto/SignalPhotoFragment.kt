package org.helpapaw.helpapaw.signalphoto

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.databinding.FragmentSignalPhotoBinding
import org.helpapaw.helpapaw.viewmodels.SignalPhotoViewModel
import org.koin.android.ext.android.inject


/**
 * Created by milen on 05/03/18.
 * Fragment to display a signal's photo
 */

const val SIGNAL_DETAILS = "signalDetails"

class SignalPhotoFragment : BaseFragment(){
    val viewModel:SignalPhotoViewModel by inject()

    internal lateinit var binding: FragmentSignalPhotoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signal_photo, container, false)
        binding.viewModel = viewModel

        var mSignal: Signal? = null
        arguments?.let{
            mSignal = arguments!!.getParcelable(SIGNAL_DETAILS)
        }

        viewModel.setPhotoUri(mSignal)
        return binding.root
    }


    fun onBackPressed() {
        activity?.finish()
    }

}// Required empty public constructor
