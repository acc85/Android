package org.helpapaw.helpapaw.koin

import android.content.Context
import android.view.LayoutInflater
import com.facebook.CallbackManager
import org.helpapaw.helpapaw.authentication.register.RegisterContract
import org.helpapaw.helpapaw.authentication.register.RegisterPresenter
import org.helpapaw.helpapaw.user.BackendlessUserManager
import org.helpapaw.helpapaw.user.UserManager
import org.helpapaw.helpapaw.db.SignalsDatabase
import org.helpapaw.helpapaw.images.ImageLoader
import org.helpapaw.helpapaw.images.PicassoImageLoader
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.repository.*
import org.helpapaw.helpapaw.settings.SettingsContract
import org.helpapaw.helpapaw.settings.SettingsPresenter
import org.helpapaw.helpapaw.signaldetails.SignalDetailsContract
import org.helpapaw.helpapaw.signaldetails.SignalDetailsPresenter
import org.helpapaw.helpapaw.signalphoto.SignalPhotoContract
import org.helpapaw.helpapaw.signalphoto.SignalPhotoPresenter
import org.helpapaw.helpapaw.signalsmap.SignalInfoWindowAdapter
import org.helpapaw.helpapaw.signalsmap.SignalsMapContract
import org.helpapaw.helpapaw.signalsmap.SignalsMapPresenter
import org.helpapaw.helpapaw.utils.Utils
import org.helpapaw.helpapaw.viewmodels.AboutViewModel
import org.helpapaw.helpapaw.viewmodels.LoginViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

var testModule = module {

    viewModel { LoginViewModel(get(),get(),get()) }

    single<CommentRepository> { BackendlessCommentRepository() }
    single<PhotoRepository> { BackendlessPhotoRepository() }
    single<SignalRepository> { BackendlessSignalRepository(get(), get()) }
    single<ImageLoader> { PicassoImageLoader() }
    single<ISettingsRepository> { SettingsRepository(androidContext().getSharedPreferences("HelpAPawSettings", Context.MODE_PRIVATE)) }

    single {CallbackManager.Factory.create()}

    single { SignalsDatabase.getDatabase(androidContext()) }

    single<UserManager> { BackendlessUserManager() }

    single { Utils(androidContext()) }

    factory { (view: SettingsContract.View) -> SettingsPresenter(view, get()) }
    factory { (view: SignalsMapContract.View) -> SignalsMapPresenter(view, get(), get(), get(), get()) }
    factory { (view: SignalDetailsContract.View) -> SignalDetailsPresenter(view, get(), get(), get(), get(), get()) }
    factory { (view: RegisterContract.View) -> RegisterPresenter(view, get(), get()) }
    factory { (view: SignalPhotoContract.View) -> SignalPhotoPresenter(view, get()) }
    factory { (signalMarkers: Map<String, Signal>, inflater: LayoutInflater) -> SignalInfoWindowAdapter(signalMarkers, inflater, get()) }

    factory<SignalPhotoContract.UserActionsListener>{ (view: SignalPhotoContract.View) -> SignalPhotoPresenter(view, get())}
}