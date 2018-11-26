package org.helpapaw.helpapaw.base

import android.app.Activity
import android.os.StrictMode
import androidx.fragment.app.Fragment
import com.backendless.Backendless
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.DaggerApplication
import org.helpapaw.helpapaw.data.user.BackendlessUserManager
import org.helpapaw.helpapaw.data.user.UserManager
import org.helpapaw.helpapaw.di.DaggerMainAppComponent
import org.helpapaw.helpapaw.utils.Injection
import org.helpapaw.helpapaw.utils.NotificationUtils
import javax.inject.Inject

class PawApplication: DaggerApplication(){

    @Inject
    lateinit var userManager: BackendlessUserManager

    override fun applicationInjector() =
        DaggerMainAppComponent.builder().application(this).build()


    companion object {
        const val BACKENDLESS_APP_ID = "BDCD56B9-351A-E067-FFA4-9EA9CF2F4000"
        const val BACKENDLESS_REST_API_KEY = "FF1687C9-961B-4388-FFF2-0C8BDC5DFB00"
        const val BACKENDLESS_ANDROID_API_KEY = "FF1687C9-961B-4388-FFF2-0C8BDC5DFB00"

        lateinit var pawApplication: PawApplication

        fun getContext(): PawApplication {
            return pawApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
        pawApplication = this
        Backendless.initApp(this, BACKENDLESS_APP_ID, BACKENDLESS_ANDROID_API_KEY)
        NotificationUtils.registerNotificationChannels(this)

        userManager.isLoggedIn(object : UserManager.LoginCallback {
            override fun onLoginSuccess() {
                // Do nothing
            }

            override fun onLoginFailure(message: String) {
                userManager.logout(object : UserManager.LogoutCallback {
                    override fun onLogoutSuccess() {}

                    override fun onLogoutFailure(message: String) {}
                })
            }
        })

        // Prevent android.os.FileUriExposedException on API 24+
        // https://stackoverflow.com/a/45569709/2781218
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

}