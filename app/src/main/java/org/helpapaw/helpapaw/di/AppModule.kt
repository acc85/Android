package org.helpapaw.helpapaw.di

import dagger.Module
import dagger.Provides
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.data.models.backendless.repositories.BackendlessPhotoRepository
import org.helpapaw.helpapaw.data.models.backendless.repositories.BackendlessSignalRepository
import org.helpapaw.helpapaw.data.user.BackendlessUserManager
import org.helpapaw.helpapaw.utils.images.PicassoImageLoader
import javax.inject.Singleton

@Module
class AppModule{

    @Singleton
    @Provides
    fun providePicasso():PicassoImageLoader{
        return PicassoImageLoader()
    }

    @Singleton
    @Provides
    fun provideUserManager():BackendlessUserManager{
        return BackendlessUserManager()
    }

    @Singleton
    @Provides
    fun providePhotoRepository():BackendlessPhotoRepository{
        return BackendlessPhotoRepository()
    }

    @Singleton
    @Provides
    fun providesSignalRepository():BackendlessSignalRepository{
        return  BackendlessSignalRepository()
    }

}
