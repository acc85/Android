package org.helpapaw.helpapaw.di

import android.content.Context
import org.helpapaw.helpapaw.images.ImageLoader
import org.helpapaw.helpapaw.images.PicassoImageLoader
import org.helpapaw.helpapaw.repository.*
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

var repositoryModule = module {

    single<CommentRepository> { BackendlessCommentRepository() }
    single<PhotoRepository> { BackendlessPhotoRepository(get()) }
    single<SignalRepository> { BackendlessSignalRepository(get(), get(),get()) }
    single<ImageLoader> { PicassoImageLoader() }
    single<ISettingsRepository> { SettingsRepository(androidContext().getSharedPreferences("HelpAPawSettings", Context.MODE_PRIVATE)) }
    single<PushNotificationsRepository>{ BackendlessPushNotificationsRepository(get()) }

}