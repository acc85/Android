package org.helpapaw.helpapaw.di

import dagger.Module
import dagger.Provides
import org.helpapaw.helpapaw.data.models.Signal

@Module
class SignalMapFragmentProviderModule{

    @SignalMapScope
    @Provides
    fun provideSignalMap():HashMap<String, Signal>{
        return HashMap()
    }
}