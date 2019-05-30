package org.helpapaw.helpapaw.signalphoto

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.base.PresenterManager
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.databinding.FragmentSignalPhotoBinding
import org.helpapaw.helpapaw.images.ImageLoader
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


/**
 * Created by milen on 05/03/18.
 * Fragment to display a signal's photo
 */

class SignalPhotoFragment : BaseFragment(), SignalPhotoContract.View {

    val signalPhotoPresenter: SignalPhotoPresenter by inject{ parametersOf(this) }
    val actionsListener: SignalPhotoContract.UserActionsListener by inject{ parametersOf(this) }
    val imageLoader: ImageLoader by inject()

    internal lateinit var binding: FragmentSignalPhotoBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signal_photo, container, false)

//        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter<Presenter>(getScreenId()) == null) {
//            signalPhotoPresenter = SignalPhotoPresenter(this)
//        } else {
//            signalPhotoPresenter = PresenterManager.getInstance().getPresenter(getScreenId())
//            signalPhotoPresenter.view = this
//        }

        var mSignal: Signal? = null
        if (arguments != null) {
            mSignal = arguments!!.getParcelable(SIGNAL_DETAILS)
        }

        actionsListener.onInitPhotoScreen(mSignal!!)

        return binding.root
    }

//    override fun getPresenter(): Presenter<*> {
//        return signalPhotoPresenter
//    }

    override fun showSignalPhoto(signal: Signal) {

        imageLoader.load(context, signal.photoUrl, binding.imgSignalPhoto, R.drawable.no_image)
    }

    fun onBackPressed() {
        activity!!.finish()
    }

    companion object {

        private val SIGNAL_DETAILS = "signalDetails"

        fun newInstance(signal: Signal): SignalPhotoFragment {
            val fragment = SignalPhotoFragment()
            val bundle = Bundle()
            bundle.putParcelable(SIGNAL_DETAILS, signal)
            fragment.arguments = bundle
            return fragment
        }
    }
}// Required empty public constructor
