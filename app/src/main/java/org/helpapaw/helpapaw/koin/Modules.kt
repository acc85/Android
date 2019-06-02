package org.helpapaw.helpapaw.koin

import android.view.LayoutInflater
import com.facebook.CallbackManager
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.places.Places
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.authentication.login.LoginFragment
import org.helpapaw.helpapaw.authentication.register.RegisterFragment
import org.helpapaw.helpapaw.authentication.register.WhyPhoneDialogFragment
import org.helpapaw.helpapaw.user.BackendlessUserManager
import org.helpapaw.helpapaw.user.UserManager
import org.helpapaw.helpapaw.db.SignalsDatabase
import org.helpapaw.helpapaw.images.ImageUtils
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.settings.SettingsFragment
import org.helpapaw.helpapaw.signaldetails.SignalDetailsContract
import org.helpapaw.helpapaw.signaldetails.SignalDetailsPresenter
import org.helpapaw.helpapaw.signalphoto.SignalPhotoFragment
import org.helpapaw.helpapaw.signalsmap.SignalInfoWindowAdapter
import org.helpapaw.helpapaw.signalsmap.SignalsMapContract
import org.helpapaw.helpapaw.utils.Utils
import org.helpapaw.helpapaw.viewmodels.*
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

var testModule = module {

    viewModel { LoginViewModel(get(),get(),get()) }
    viewModel { RegisterViewModel(get(),get()) }
    viewModel {
        SettingsViewModel(
                androidContext().getString(R.string.text_settings),
                androidContext().getString(R.string.radius_output_single),
                androidContext().getString(R.string.radius_output),
                androidContext().getString(R.string.timeout_output_single),
                androidContext().getString(R.string.timeout_output), get())
    }

    viewModel{ SignalPhotoViewModel(get(),get())}

    viewModel{ SignalsMapViewModel(get()) }


    single { LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval((30 * 1000).toLong())
        }

    single { GoogleApiClient.Builder(androidContext())
            .addApi(LocationServices.API)
            .addApi(Places.GEO_DATA_API)
            .build() }

    single { LocationServices.getFusedLocationProviderClient(androidContext()) }

    single { LocationSettingsRequest.Builder().addLocationRequest(LocationRequest()).build() }

    single {CallbackManager.Factory.create()}

    single { SignalsDatabase.getDatabase(androidContext()) }

    single<UserManager> { BackendlessUserManager() }

    single { Utils(androidContext()) }
    single { ImageUtils() }

    factory { (view: SignalDetailsContract.View) -> SignalDetailsPresenter(view, get(), get(), get(), get(), get()) }
    factory { (signalMarkers: Map<String, Signal>, inflater: LayoutInflater) -> SignalInfoWindowAdapter(signalMarkers, inflater, get()) }


    factory{ LoginFragment()}
    factory{ RegisterFragment()}
    factory{ WhyPhoneDialogFragment() }
    factory{ SettingsFragment() }
    factory{ SignalPhotoFragment() }
}