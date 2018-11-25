package org.helpapaw.helpapaw.di

import com.facebook.CallbackManager
import com.facebook.login.Login
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import org.helpapaw.helpapaw.authentication.login.LoginFragment
import javax.inject.Singleton

@Module
class AuthenticatorActivityComponent{

    @Provides
    fun provdeCallbackManager():CallbackManager{
        return CallbackManager.Factory.create()
    }


}