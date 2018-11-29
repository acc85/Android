package org.helpapaw.helpapaw.di

import androidx.room.Room
import dagger.Module
import dagger.Provides
import org.helpapaw.helpapaw.base.PawApplication
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.data.models.backendless.repositories.BackendlessPhotoRepository
import org.helpapaw.helpapaw.data.models.backendless.repositories.BackendlessSignalRepository
import org.helpapaw.helpapaw.data.user.BackendlessUserManager
import org.helpapaw.helpapaw.db.SignalDao
import org.helpapaw.helpapaw.db.SignalsDatabase
import org.helpapaw.helpapaw.utils.Utils
import org.helpapaw.helpapaw.utils.images.ImageUtils
import org.helpapaw.helpapaw.utils.images.PicassoImageLoader
import javax.inject.Singleton

@Module
class AppModule{

    @Singleton
    @Provides
    fun provideUtils(application: PawApplication): Utils {
        return Utils(application);
    }

    @Singleton
    @Provides
    fun provideImageUtils():ImageUtils{
        return ImageUtils()
    }


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
    fun provideSignalsDao(application: PawApplication):SignalDao{
        return Room.databaseBuilder(application.applicationContext, SignalsDatabase::class.java, "signals.db").allowMainThreadQueries().build().signalDao()
    }

    @Singleton
    @Provides
    fun providePhotoRepository(imageUtils: ImageUtils):BackendlessPhotoRepository{
        return BackendlessPhotoRepository(imageUtils)
    }

    @Singleton
    @Provides
    fun providesSignalRepository(application: PawApplication, signalDao: SignalDao):BackendlessSignalRepository{
        return BackendlessSignalRepository(application,signalDao)
    }

}
