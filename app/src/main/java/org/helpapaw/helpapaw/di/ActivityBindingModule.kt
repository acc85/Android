package org.helpapaw.helpapaw.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.helpapaw.helpapaw.authentication.AuthenticationActivity
import dagger.android.support.AndroidSupportInjectionModule



@Module
abstract class ActivityBindingModule{

    @ContributesAndroidInjector(modules = [AuthenticatorActivityComponent::class])
    abstract fun contributeAuthenticationActivity():AuthenticationActivity
}