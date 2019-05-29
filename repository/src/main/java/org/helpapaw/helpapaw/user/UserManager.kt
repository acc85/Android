package org.helpapaw.helpapaw.user

import kotlinx.coroutines.Deferred

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

    fun getHasAcceptedPrivacyPolicy(userPropertyCallback: UserPropertyCallback)
    fun setHasAcceptedPrivacyPolicy(value: Boolean, userPropertyCallback: UserPropertyCallback)
//    suspend fun getHasAcceptedPrivacyPolicy():Deferred<UserPropertyResult>

//    suspend fun setHasAcceptedPrivacyPolicy(value: Boolean): Deferred<UserPropertyResult>

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

    interface UserPropertyCallback {
        fun onResult(userPropertyResult: UserPropertyResult)
    }

    interface GetUserPropertyCallback {
        fun onSuccess(value: Any)
        fun onFailure(message: String?)
    }

    sealed class UserPropertyResult{
        data class Success(val value:Any=""):UserPropertyResult()
        data class Failed(var message: String?):UserPropertyResult()
    }
    interface SetUserPropertyCallback {
        fun onSuccess()
        fun onFailure(message: String?)
    }
}
