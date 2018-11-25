package org.helpapaw.helpapaw.di

import dagger.Binds
import dagger.Module
import org.helpapaw.helpapaw.authentication.login.LoginPresenter
import org.helpapaw.helpapaw.base.IPresenter

@Module
abstract class LoginFragmentModule {

    @Binds
    abstract fun provideLoginPresenter(loginPresenter: LoginPresenter): IPresenter

}