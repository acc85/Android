package org.helpapaw.helpapaw.di

import com.google.android.gms.maps.GoogleMap
import dagger.Binds
import dagger.Module
import org.helpapaw.helpapaw.base.IPresenter
import org.helpapaw.helpapaw.data.models.backendless.repositories.BackendlessCommentRepository
import org.helpapaw.helpapaw.data.models.backendless.repositories.CommentRepository
import org.helpapaw.helpapaw.data.user.BackendlessUserManager
import org.helpapaw.helpapaw.signaldetails.SignalDetailsPresenter
import org.helpapaw.helpapaw.signalsmap.SignalInfoWindowAdapter
import javax.inject.Inject

@Module
abstract class SignalDetailFragmentModule{

    @Binds
    abstract fun providePresenter(signalDetailPresenter:SignalDetailsPresenter):IPresenter

    @Binds
    abstract fun provideCommentsRepository(commentsProvider:BackendlessCommentRepository):CommentRepository

    @Binds
    abstract fun provideSignalInfoWindowAdapter(signalInfoWindowAdapter: SignalInfoWindowAdapter): GoogleMap.InfoWindowAdapter

}