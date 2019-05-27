package org.helpapaw.helpapaw.base

import android.app.Application
import android.content.Context
import android.os.StrictMode
import com.backendless.Backendless
import org.helpapaw.helpapaw.user.UserManager
import org.helpapaw.helpapaw.koin.testModule
import org.helpapaw.helpapaw.utils.NotificationUtils
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class PawApplication:Application(){

    val userManager:UserManager by inject()

    companion object{
        const val BACKENDLESS_APP_ID: String = "BDCD56B9-351A-E067-FFA4-9EA9CF2F4000"
        const val BACKENDLESS_REST_API_KEY:String = "FF1687C9-961B-4388-FFF2-0C8BDC5DFB00"
        const val BACKENDLESS_ANDROID_API_KEY = "FF1687C9-961B-4388-FFF2-0C8BDC5DFB00"
        private var isTestEnvironment: Boolean? = null
        private var pawApplication: PawApplication? = null

        const val IS_TEST_ENVIRONMENT_KEY = "IS_TEST_ENVIRONMENT_KEY"
        fun getIsTestEnvironment(): Boolean? {
            return isTestEnvironment
        }

        fun setIsTestEnvironment(isTestEnvironment: Boolean?) {
            PawApplication.isTestEnvironment = isTestEnvironment
            pawApplication?.saveIsTestEnvironment(isTestEnvironment)
        }

        fun getContext(): PawApplication {
            return pawApplication!!
        }

    }


    override fun onCreate() {
        super.onCreate()
        var list = listOf(testModule)
        startKoin{
            androidContext(this@PawApplication)
            modules(list)
        }
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        pawApplication = this
        isTestEnvironment = loadIsTestEnvironment()
        Backendless.initApp(this, BACKENDLESS_APP_ID, BACKENDLESS_ANDROID_API_KEY)
        NotificationUtils.registerNotificationChannels(this)

        // This is done in order to handle the situation where user token is saved on the device but is invalidated on the server
        userManager.isLoggedIn(object : UserManager.LoginCallback {
            override fun onLoginSuccess() {
                // Do nothing
            }

            override fun onLoginFailure(message: String?) {
                message?.let{
                    userManager.logout(object : UserManager.LogoutCallback {
                        override fun onLogoutSuccess() {}
                        override fun onLogoutFailure(message: String) {}
                    })
                }

            }
        })
    }

    private fun loadIsTestEnvironment(): Boolean {
        val prefs = getSharedPreferences("HelpAPaw", Context.MODE_PRIVATE)
        return prefs.getBoolean(IS_TEST_ENVIRONMENT_KEY, false)
    }

    private fun saveIsTestEnvironment(isTestEnvironment: Boolean?) {
        val prefs = pawApplication?.getSharedPreferences("HelpAPaw", Context.MODE_PRIVATE)
        prefs!!.edit().putBoolean(IS_TEST_ENVIRONMENT_KEY, isTestEnvironment!!).apply()
    }
}