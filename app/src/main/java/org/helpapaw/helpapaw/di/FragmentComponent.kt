package org.helpapaw.helpapaw.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.helpapaw.helpapaw.authentication.login.LoginFragment
import org.helpapaw.helpapaw.authentication.register.RegisterFragment
import org.helpapaw.helpapaw.signaldetails.SignalDetailsFragment
import org.helpapaw.helpapaw.signalphoto.SignalPhotoFragment
import org.helpapaw.helpapaw.signalsmap.SignalsMapFragment

@Module
abstract class FragmentComponent{

    @ContributesAndroidInjector(modules = [RegisterFragmentModule::class])
    abstract fun provideRegisterFragment():RegisterFragment

    @ContributesAndroidInjector(modules = [LoginFragmentModule::class])
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector(modules = [SignalFragmentModule::class])
    abstract fun contributeSignalDetailFragment():SignalDetailsFragment

    @ContributesAndroidInjector(modules  = [SignalPhotoFragmentModule::class])
    abstract fun contributeSignalPhotoFragment():SignalPhotoFragment

    @ContributesAndroidInjector(modules = [SignalMapFragmentModule::class])
    abstract fun contributeSignalMapFragment():SignalsMapFragment
}