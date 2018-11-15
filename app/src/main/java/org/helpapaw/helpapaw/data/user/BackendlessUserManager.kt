package org.helpapaw.helpapaw.data.user

import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.local.UserTokenStorageFactory
import com.facebook.login.LoginManager

class BackendlessUserManager: UserManager{

    companion object {
        private const val USER_EMAIL_FIELD = "email"
        private const val USER_NAME_FIELD = "name"
        private const val USER_PHONE_NUMBER_FIELD = "phoneNumber"
    }


    override fun login(email: String?, password: String?, loginCallback: UserManager.LoginCallback?) {
        Backendless.UserService.login(email, password, object : AsyncCallback<BackendlessUser> {
            override fun handleResponse(user: BackendlessUser) {
                loginCallback?.onLoginSuccess()
            }

            override fun handleFault(fault: BackendlessFault) {
                loginCallback?.onLoginFailure(fault.message)
            }
        }, true)
    }

    override fun register(email: String?, password: String?, name: String?, phoneNumber: String?, registrationCallback: UserManager.RegistrationCallback?) {
        val user = BackendlessUser()
        user.setProperty(USER_EMAIL_FIELD, email)
        user.setProperty(USER_NAME_FIELD, name)
        user.setProperty(USER_PHONE_NUMBER_FIELD, phoneNumber)
        user.password = password

        Backendless.UserService.register(user, object : AsyncCallback<BackendlessUser> {
            override fun handleResponse(registeredUser: BackendlessUser) {
                registrationCallback?.onRegistrationSuccess()
            }

            override fun handleFault(fault: BackendlessFault) {
                registrationCallback?.onRegistrationFailure(fault.message)
            }
        })
    }

    override fun logout(logoutCallback: UserManager.LogoutCallback?) {
        Backendless.UserService.logout(object : AsyncCallback<Void> {
            override fun handleResponse(response: Void?) {
                LoginManager.getInstance().logOut()
                logoutCallback?.onLogoutSuccess()
            }

            override fun handleFault(fault: BackendlessFault) {
                logoutCallback?.onLogoutFailure(fault.message)
            }
        })
    }

    override fun isLoggedIn(loginCallback: UserManager.LoginCallback?) {
        Backendless.UserService.isValidLogin(object : AsyncCallback<Boolean> {
            override fun handleResponse(isValidLogin: Boolean?) {
                if (isValidLogin != null && isValidLogin) {
                    if (Backendless.UserService.CurrentUser() == null) {
                        val currentUserId = Backendless.UserService.loggedInUser()
                        if (currentUserId != "") {
                            Backendless.UserService.findById(currentUserId, object : AsyncCallback<BackendlessUser> {
                                override fun handleResponse(currentUser: BackendlessUser?) {
                                    if (currentUser != null) {
                                        Backendless.UserService.setCurrentUser(currentUser)
                                        loginCallback?.onLoginSuccess()
                                    } else {
                                        loginCallback?.onLoginFailure("Error")
                                    }
                                }

                                override fun handleFault(fault: BackendlessFault) {
                                    loginCallback?.onLoginFailure(fault.message)
                                }
                            })
                        }
                    } else {
                        loginCallback?.onLoginSuccess()
                    }
                } else {
                    loginCallback?.onLoginFailure("error")
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                loginCallback?.onLoginFailure(fault.message)
            }
        })
    }

    override fun isLoggedIn(): Boolean {
        val userToken = getUserToken()

        return !userToken.isEmpty()
    }

    override fun getUserToken(): String {
        return UserTokenStorageFactory.instance().storage.get()
    }

}