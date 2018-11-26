package org.helpapaw.helpapaw.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.helpapaw.helpapaw.authentication.AuthenticationActivity
import dagger.android.support.AndroidSupportInjectionModule
import org.helpapaw.helpapaw.base.BaseActivity
import org.helpapaw.helpapaw.base.BaseDaggerActivity
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.signaldetails.SignalDetailsActivity
import org.helpapaw.helpapaw.signalsmap.SignalsMapActivity


@Module
abstract class ActivityBindingModule{

    @ContributesAndroidInjector(modules = [AuthenticatorActivityComponent::class])
    abstract fun contributeAuthenticationActivity():AuthenticationActivity

    @ContributesAndroidInjector()
    abstract fun contributeBaseDaggerActivity(): BaseDaggerActivity

    @ContributesAndroidInjector()
    abstract fun contributeSignalDetailsActivity(): SignalDetailsActivity

    @ContributesAndroidInjector()
    abstract fun contributeSignalsMapActivity(): SignalsMapActivity
}