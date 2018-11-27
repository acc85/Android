package org.helpapaw.helpapaw.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.helpapaw.helpapaw.signalsmap.SignalsMapFragment

@Module
abstract class SignalMapComponent{

    @TestScope
    @ContributesAndroidInjector(modules = [SignalMapFragmentModule::class,SignalMapFragmentProviderModule::class])
    abstract fun contributeSignalMapFragment(): SignalsMapFragment

}