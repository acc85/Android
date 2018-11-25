package org.helpapaw.helpapaw.di

import dagger.Binds
import dagger.Module
import org.helpapaw.helpapaw.base.IPresenter
import org.helpapaw.helpapaw.signaldetails.SignalDetailsPresenter

@Module
abstract class SignalFragmentModule{

    @Binds
    abstract fun providePresenter(signalDetailPresenter:SignalDetailsPresenter):IPresenter

}