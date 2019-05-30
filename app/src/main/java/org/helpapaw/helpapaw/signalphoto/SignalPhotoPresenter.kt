package org.helpapaw.helpapaw.signalphoto

import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.repository.PhotoRepository

/**
 * Created by milen on 05/03/18.
 * The Presenter for showing a signal's photo on full screen
 */

class SignalPhotoPresenter(
        view: SignalPhotoContract.View,
        private val photoRepository: PhotoRepository
) : Presenter<SignalPhotoContract.View>(view), SignalPhotoContract.UserActionsListener {

    override fun onInitPhotoScreen(signal: Signal) {
        if (signal != null) {
            signal.photoUrl = photoRepository.getPhotoUrl(signal.id)
            view?.showSignalPhoto(signal)
        }
    }
}
