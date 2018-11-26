package org.helpapaw.helpapaw.di

import dagger.Binds
import dagger.Module
import org.helpapaw.helpapaw.authentication.login.LoginPresenter
import org.helpapaw.helpapaw.base.IPresenter
import org.helpapaw.helpapaw.data.models.backendless.repositories.BackendlessFacebookLogin
import org.helpapaw.helpapaw.data.models.backendless.repositories.FacebookLogin

@Module
abstract class LoginFragmentModule {

    @Binds
    abstract fun providesFacebookLogin(facebookLogin:BackendlessFacebookLogin): FacebookLogin

    @Binds
    abstract fun provideLoginPresenter(loginPresenter: LoginPresenter): IPresenter

}