package org.helpapaw.helpapaw.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import org.helpapaw.helpapaw.base.IPresenter
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.signalsmap.SignalsMapPresenter

@Module
abstract class SignalMapFragmentModule{

    @TestScope
    @Binds
    abstract fun bindSignalMapPresenter(signalsMapPresenter: SignalsMapPresenter):IPresenter
}
