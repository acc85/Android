package org.helpapaw.helpapaw.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.helpapaw.helpapaw.authentication.AuthenticationActivity
import org.helpapaw.helpapaw.base.BaseActivity
import org.helpapaw.helpapaw.signaldetails.SignalDetailsActivity
import org.helpapaw.helpapaw.signalsmap.SignalsMapActivity


@Module
abstract class ActivityBindingModule{

    @ContributesAndroidInjector(modules = [AuthenticatorActivityComponent::class])
    abstract fun contributeAuthenticationActivity():AuthenticationActivity

    @ContributesAndroidInjector()
    abstract fun contributeBaseDaggerActivity(): BaseActivity

    @ContributesAndroidInjector()
    abstract fun contributeSignalDetailsActivity(): SignalDetailsActivity

    @ContributesAndroidInjector()
    abstract fun contributeSignalsMapActivity(): SignalsMapActivity
}