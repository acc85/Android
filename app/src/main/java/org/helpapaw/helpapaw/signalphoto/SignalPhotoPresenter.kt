package org.helpapaw.helpapaw.signalphoto

import io.fabric.sdk.android.services.concurrency.AsyncTask.init
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.data.models.backendless.repositories.PhotoRepository
import org.helpapaw.helpapaw.utils.Injection

class SignalPhotoPresenter(override var view: SignalPhotoContract.View?) : Presenter<SignalPhotoContract.View>(view),  SignalPhotoContract.UserActionsListener{

    private val photoRepository: PhotoRepository by lazy{
        Injection.getPhotoRepositoryInstance()
    }

    override fun onInitPhotoScreen(signal: Signal) {
        signal.photoUrl = photoRepository.getPhotoUrl(signal.id)
        view?.showSignalPhoto(signal)
    }


}