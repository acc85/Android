package org.helpapaw.helpapaw.data.user

interface UserManager {

    fun isLoggedIn():Boolean

    fun getUserToken():String

    fun login(email: String?, password: String?, loginCallback: LoginCallback?)

    fun register(email: String?, password: String?, name: String?, phoneNumber: String?, registrationCallback: RegistrationCallback?)

    fun logout(logoutCallback: LogoutCallback?)

    fun isLoggedIn(loginCallback: LoginCallback?)

    interface LoginCallback {

        fun onLoginSuccess()

        fun onLoginFailure(message: String)
    }

    interface RegistrationCallback {

        fun onRegistrationSuccess()

        fun onRegistrationFailure(message: String)
    }

    interface LogoutCallback {

        fun onLogoutSuccess()

        fun onLogoutFailure(message: String)
    }
}