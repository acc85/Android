package org.helpapaw.helpapaw.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.helpapaw.helpapaw.utils.services.BackgroundCheckJobService

@Module
abstract class JobBindingComponent{

    @ContributesAndroidInjector
    abstract fun provideBackgroundCheckJobService():BackgroundCheckJobService

}