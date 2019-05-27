package org.helpapaw.helpapaw.data.user

import android.util.Log
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessException
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.local.UserTokenStorageFactory
import com.facebook.login.LoginManager

/**
 * Created by iliyan on 7/25/16
 */
class BackendlessUserManager : UserManager {

    override val userToken: String
        get() = UserTokenStorageFactory.instance().storage.get()

    override val isLoggedIn: Boolean
        get() {
            val userToken = userToken

            return userToken != null && userToken != ""
        }

    override fun login(email: String, password: String, loginCallback: UserManager.LoginCallback) {
        Backendless.UserService.login(email, password, object : AsyncCallback<BackendlessUser> {
            override fun handleResponse(user: BackendlessUser) {
                Backendless.UserService.setCurrentUser(user)
                loginCallback.onLoginSuccess()
            }

            override fun handleFault(fault: BackendlessFault) {
                loginCallback.onLoginFailure(fault.message)
            }
        }, true)
    }

    override fun loginWithFacebook(accessToken: String, loginCallback: UserManager.LoginCallback) {
        Backendless.UserService.loginWithFacebookSdk(accessToken, object : AsyncCallback<BackendlessUser> {
            override fun handleResponse(user: BackendlessUser) {
                Backendless.UserService.setCurrentUser(user)
                loginCallback.onLoginSuccess()
            }

            override fun handleFault(fault: BackendlessFault) {
                loginCallback.onLoginFailure(fault.message)
            }
        },
                true)
    }

    override fun register(email: String, password: String, name: String, phoneNumber: String, registrationCallback: UserManager.RegistrationCallback) {
        val user = BackendlessUser()
        user.setProperty(USER_EMAIL_FIELD, email)
        user.setProperty(USER_NAME_FIELD, name)
        user.setProperty(USER_PHONE_NUMBER_FIELD, phoneNumber)
        user.setProperty(USER_ACCEPTED_PRIVACY_POLICY_FIELD, true)
        user.password = password

        Backendless.UserService.register(user, object : AsyncCallback<BackendlessUser> {
            override fun handleResponse(registeredUser: BackendlessUser) {
                registrationCallback.onRegistrationSuccess()
            }

            override fun handleFault(fault: BackendlessFault) {
                registrationCallback.onRegistrationFailure(fault.message)
            }
        })
    }

    override fun logout(logoutCallback: UserManager.LogoutCallback) {
        Backendless.UserService.logout(object : AsyncCallback<Void> {
            override fun handleResponse(response: Void) {
                LoginManager.getInstance().logOut()
                logoutCallback.onLogoutSuccess()
            }

            override fun handleFault(fault: BackendlessFault) {
                logoutCallback.onLogoutFailure(fault.message?:"")
            }
        })
    }

    override fun isLoggedIn(loginCallback: UserManager.LoginCallback) {
        Backendless.UserService.isValidLogin(object : AsyncCallback<Boolean> {
            override fun handleResponse(isValidLogin: Boolean?) {
                if (isValidLogin != null && isValidLogin) {
                    if (Backendless.UserService.CurrentUser() == null) {
                        //https://support.backendless.com/t/userservice-currentuser-is-null-even-if-usertokenstoragefactory-retruns-token/3239
                        val currentUserId = Backendless.UserService.loggedInUser()
                        if (currentUserId != "") {
                            Backendless.UserService.findById(currentUserId, object : AsyncCallback<BackendlessUser> {
                                override fun handleResponse(currentUser: BackendlessUser?) {
                                    if (currentUser != null) {
                                        Backendless.UserService.setCurrentUser(currentUser)
                                        loginCallback.onLoginSuccess()
                                    } else {
                                        loginCallback.onLoginFailure(null)
                                    }
                                }

                                override fun handleFault(fault: BackendlessFault) {
                                    loginCallback.onLoginFailure(fault.message)
                                }
                            })
                        }
                    } else {
                        loginCallback.onLoginSuccess()
                    }
                } else {
                    loginCallback.onLoginFailure(null)
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                loginCallback.onLoginFailure(fault.message)
            }
        })
    }

    override fun setHasAcceptedPrivacyPolicy(value: Boolean, setUserPropertyCallback: UserManager.SetUserPropertyCallback) {
        try {
            Backendless.UserService.CurrentUser().setProperty(USER_ACCEPTED_PRIVACY_POLICY_FIELD, true)
            Backendless.UserService.update(Backendless.UserService.CurrentUser(), object : AsyncCallback<BackendlessUser> {
                override fun handleResponse(response: BackendlessUser) {
                    setUserPropertyCallback.onSuccess()
                }

                override fun handleFault(fault: BackendlessFault) {
                    setUserPropertyCallback.onFailure(fault.message)
                }
            })
        } catch (exception: BackendlessException) {
            // update failed, to get the error code, call exception.getFault().getCode()
            Log.e(BackendlessUserManager::class.java.simpleName, exception.message)
        }

    }

    override fun getHasAcceptedPrivacyPolicy(getUserPropertyCallback: UserManager.GetUserPropertyCallback) {

        //https://support.backendless.com/t/userservice-currentuser-is-null-even-if-usertokenstoragefactory-retruns-token/3239
        val currentUserId = Backendless.UserService.loggedInUser()
        if (currentUserId != "") {
            Backendless.UserService.findById(currentUserId, object : AsyncCallback<BackendlessUser> {
                override fun handleResponse(currentUser: BackendlessUser) {
                    var result: Any? = false
                    val value = currentUser.getProperty(USER_ACCEPTED_PRIVACY_POLICY_FIELD)
                    if (value != null) {
                        result = value
                    }
                    getUserPropertyCallback.onSuccess(result!!)
                }

                override fun handleFault(fault: BackendlessFault) {
                    getUserPropertyCallback.onFailure(fault.message)
                }
            })
        } else {
            getUserPropertyCallback.onFailure("User not logged in!")
        }
    }

    override fun getUserName(getUserPropertyCallback: UserManager.GetUserPropertyCallback) {
        val currentUserId = Backendless.UserService.loggedInUser()
        if (currentUserId != "") {
            Backendless.UserService.findById(currentUserId, object : AsyncCallback<BackendlessUser> {
                override fun handleResponse(currentUser: BackendlessUser) {
                    var result: Any? = false
                    val value = currentUser.getProperty(USER_NAME_FIELD)
                    if (value != null) {
                        result = value
                    }
                    getUserPropertyCallback.onSuccess(result!!)
                }

                override fun handleFault(fault: BackendlessFault) {
                    getUserPropertyCallback.onFailure(fault.message)
                }
            })
        } else {
            getUserPropertyCallback.onFailure("User not logged in!")
        }
    }

    companion object {

        private val USER_EMAIL_FIELD = "email"
        private val USER_NAME_FIELD = "name"
        private val USER_PHONE_NUMBER_FIELD = "phoneNumber"
        private val USER_ACCEPTED_PRIVACY_POLICY_FIELD = "acceptedPrivacyPolicy"
    }
}
