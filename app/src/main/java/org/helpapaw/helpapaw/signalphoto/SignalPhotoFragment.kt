package org.helpapaw.helpapaw.signalphoto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.databinding.FragmentSignalPhotoBinding
import org.helpapaw.helpapaw.utils.images.PicassoImageLoader
import javax.inject.Inject

class SignalPhotoFragment:BaseFragment(),SignalPhotoContract.View {

    companion object {
        private const val SIGNAL_DETAILS = "signalDetails"

        fun newInstance(signal: Signal): SignalPhotoFragment {
            val fragment = SignalPhotoFragment()
            val bundle = Bundle()
            bundle.putParcelable(SIGNAL_DETAILS, signal)
            fragment.arguments = bundle
            return fragment
        }

    }

    @Inject
    lateinit internal var signalPhotoPresenter: SignalPhotoPresenter

    @Inject
    lateinit var imageLoader:PicassoImageLoader

    internal var actionsListener: SignalPhotoContract.UserActionsListener? = null

    internal lateinit var binding: FragmentSignalPhotoBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signal_photo, container, false)
        actionsListener = signalPhotoPresenter
        var mSignal: Signal? = null
        if (arguments != null) {
            mSignal = arguments!!.getParcelable(SIGNAL_DETAILS)
        }

        actionsListener?.onInitPhotoScreen(mSignal!!)

        return binding.root
    }

    override fun getPresenter(): Presenter<*>? {
        return signalPhotoPresenter
    }

    override fun showSignalPhoto(signal: Signal) {
        imageLoader.load(context, signal.photoUrl, binding.imgSignalPhoto, R.drawable.no_image)
    }

    fun onBackPressed() {
        activity!!.finish()
    }

}