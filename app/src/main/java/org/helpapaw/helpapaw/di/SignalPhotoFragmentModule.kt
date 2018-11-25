package org.helpapaw.helpapaw.di

import dagger.Binds
import dagger.Module
import org.helpapaw.helpapaw.base.IPresenter
import org.helpapaw.helpapaw.data.models.backendless.repositories.BackendlessPhotoRepository
import org.helpapaw.helpapaw.data.models.backendless.repositories.PhotoRepository
import org.helpapaw.helpapaw.signalphoto.SignalPhotoPresenter

@Module
abstract class SignalPhotoFragmentModule{

    @Binds
    abstract fun bindSignalPhotoPresenter(signalPhotoPresenter: SignalPhotoPresenter): IPresenter

    @Binds
    abstract fun bindBackendlessPhotoRepository(backendlessPhotoRepository: BackendlessPhotoRepository): PhotoRepository
}