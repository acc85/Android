package org.helpapaw.helpapaw.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.helpapaw.helpapaw.authentication.login.LoginFragment
import org.helpapaw.helpapaw.authentication.register.RegisterFragment

@Module
abstract class FragmentComponent{

    @ContributesAndroidInjector(modules = [RegisterFragmentModule::class])
    abstract fun provideRegisterFragment():RegisterFragment

    @ContributesAndroidInjector(modules = [LoginFragmentModule::class])
    abstract fun contributeLoginFragment(): LoginFragment
}