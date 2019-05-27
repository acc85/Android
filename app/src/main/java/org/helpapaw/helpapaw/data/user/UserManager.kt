package org.helpapaw.helpapaw.data.user

/**
 * Created by iliyan on 7/25/16
 */
interface UserManager {

    val userToken: String

    val isLoggedIn: Boolean

    fun login(email: String, password: String, loginCallback: LoginCallback)

    fun loginWithFacebook(accessToken: String, loginCallback: LoginCallback)

    fun register(email: String, password: String, name: String, phoneNumber: String, registrationCallback: RegistrationCallback)

    fun logout(logoutCallback: LogoutCallback)

    fun isLoggedIn(loginCallback: LoginCallback)

    fun getUserName(getUserPropertyCallback: GetUserPropertyCallback)

    fun getHasAcceptedPrivacyPolicy(getUserPropertyCallback: GetUserPropertyCallback)
    fun setHasAcceptedPrivacyPolicy(value: Boolean, setUserPropertyCallback: SetUserPropertyCallback)

    interface LoginCallback {
        fun onLoginSuccess()
        fun onLoginFailure(message: String?)
    }

    interface RegistrationCallback {
        fun onRegistrationSuccess()
        fun onRegistrationFailure(message: String?)
    }

    interface LogoutCallback {
        fun onLogoutSuccess()
        fun onLogoutFailure(message: String)
    }

    interface GetUserPropertyCallback {
        fun onSuccess(value: Any)
        fun onFailure(message: String?)
    }

    interface SetUserPropertyCallback {
        fun onSuccess()
        fun onFailure(message: String?)
    }
}
