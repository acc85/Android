package org.helpapaw.helpapaw.koin

import android.content.Context
import org.helpapaw.helpapaw.authentication.login.LoginContract
import org.helpapaw.helpapaw.authentication.login.LoginPresenter
import org.helpapaw.helpapaw.data.repositories.*
import org.helpapaw.helpapaw.data.user.BackendlessUserManager
import org.helpapaw.helpapaw.data.user.UserManager
import org.helpapaw.helpapaw.db.SignalsDatabase
import org.helpapaw.helpapaw.settings.SettingsContract
import org.helpapaw.helpapaw.settings.SettingsPresenter
import org.helpapaw.helpapaw.signalsmap.SignalsMapContract
import org.helpapaw.helpapaw.signalsmap.SignalsMapPresenter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

var testModule = module{

    single { BackendlessCommentRepository() }

    single<ISettingsRepository> { SettingsRepository(androidContext().getSharedPreferences("HelpAPawSettings", Context.MODE_PRIVATE))}

    factory { (view : SettingsContract.View) ->SettingsPresenter(view, get())}

    single { SignalsDatabase.getDatabase(androidContext())}

    single<SignalRepository> { BackendlessSignalRepository(get())}

    single { (view: SignalsMapContract.View) -> SignalsMapPresenter(view,get()) }

    single<UserManager> { BackendlessUserManager() }

    single{ (view: LoginContract.View) -> LoginPresenter(view, get()) }

}