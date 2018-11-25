package org.helpapaw.helpapaw.di

import dagger.Binds
import dagger.Module
import org.helpapaw.helpapaw.authentication.register.RegisterPresenter
import org.helpapaw.helpapaw.base.IPresenter

@Module
abstract class RegisterFragmentModule{

    @Binds
    abstract fun provideRegisterPresenter(registerPresenter: RegisterPresenter): IPresenter

}