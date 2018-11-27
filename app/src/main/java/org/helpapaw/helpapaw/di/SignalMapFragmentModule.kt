package org.helpapaw.helpapaw.di

import dagger.Binds
import dagger.Module
import org.helpapaw.helpapaw.base.IPresenter
import org.helpapaw.helpapaw.signalsmap.SignalsMapPresenter

@Module
abstract class SignalMapFragmentModule{

    @SignalMapScope
    @Binds
    abstract fun bindSignalMapPresenter(signalsMapPresenter: SignalsMapPresenter):IPresenter
}
