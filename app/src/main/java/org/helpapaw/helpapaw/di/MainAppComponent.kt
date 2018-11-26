package org.helpapaw.helpapaw.di

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import org.helpapaw.helpapaw.base.PawApplication
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class,FragmentComponent::class,ActivityBindingModule::class,JobBindingComponent::class,AndroidInjectionModule::class,AndroidSupportInjectionModule::class])
interface MainAppComponent:AndroidInjector<PawApplication> {


    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: PawApplication):Builder
        fun build():MainAppComponent
    }

    override fun inject(pawApplication: PawApplication)


}
