package org.helpapaw.helpapaw.authentication.login

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.authentication.PrivacyPolicyConfirmationContract
import org.helpapaw.helpapaw.authentication.PrivacyPolicyConfirmationGetter
import org.helpapaw.helpapaw.base.PawApplication
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.user.UserManager
import org.helpapaw.helpapaw.utils.Utils

/**
 * Created by iliyan on 7/25/16
 */
class LoginPresenter internal constructor(
        view: LoginContract.View,val userManager: UserManager,
        val utils:Utils
) : Presenter<LoginContract.View>(view), LoginContract.UserActionsListener, PrivacyPolicyConfirmationContract.Obtain, PrivacyPolicyConfirmationContract.UserResponse {

    private var showProgressBar: Boolean = false

    private val isViewAvailable: Boolean
        get() = view != null && view!!.isActive()

    init {
        showProgressBar = false
    }

    override fun onInitLoginScreen() {
        setProgressIndicator(showProgressBar)
    }

    override fun onLoginButtonClicked(email: String, password: String) {
        view?.clearErrorMessages()

        if (isEmpty(email) || !utils.isEmailValid(email)) {
            view?.showEmailErrorMessage()
            return
        }

        if (isEmpty(password) || password.length < MIN_PASS_LENGTH) {
            view?.showPasswordErrorMessage()
            return
        }

        view?.hideKeyboard()
        setProgressIndicator(true)
        attemptToLogin(email, password)
    }

    private fun attemptToLogin(email: String, password: String) {
        if (utils.hasNetworkConnection()) {
            userManager.login(email, password, object : UserManager.LoginCallback {
                override fun onLoginSuccess() {
                    this@LoginPresenter.onLoginSuccess()
                }

                override fun onLoginFailure(message: String?) {
                    this@LoginPresenter.onLoginFailure(message)
                }
            })
        } else {
            view?.showNoInternetMessage()
            setProgressIndicator(false)
        }
    }

    private fun onLoginSuccess() {
        setProgressIndicator(true)
        userManager.getHasAcceptedPrivacyPolicy(object : UserManager.GetUserPropertyCallback {
            override fun onSuccess(hasAcceptedPrivacyPolicy: Any) {

                try {
                    val accepted = hasAcceptedPrivacyPolicy as Boolean
                    if (!accepted) {
                        val privacyPolicyConfirmationGetter = PrivacyPolicyConfirmationGetter(this@LoginPresenter, PawApplication.getContext())
                        privacyPolicyConfirmationGetter.execute()
                    } else {
                        if (!isViewAvailable) return
                        view?.closeLoginScreen()
                    }
                } catch (ignored: Exception) {
                    if (!isViewAvailable) return
                    view?.closeLoginScreen()
                }

            }

            override fun onFailure(message: String?) {
                if (!isViewAvailable) return
                setProgressIndicator(false)
                view?.showErrorMessage(message)
            }
        })
    }

    private fun onLoginFailure(message: String?) {
        if (!isViewAvailable) return
        setProgressIndicator(false)
        view?.showErrorMessage(message)
    }

    private fun setProgressIndicator(active: Boolean) {
        view?.setProgressIndicator(active)
        this.showProgressBar = active
    }

    override fun onRegisterButtonClicked() {
        view?.openRegisterScreen()
    }

    private fun isEmpty(value: String?): Boolean {
        return !(value != null && value.length > 0)
    }

    override fun onLoginFbSuccess(accessToken: String) {
        if (utils.hasNetworkConnection()) {
            setProgressIndicator(true)
            userManager.loginWithFacebook(accessToken, object : UserManager.LoginCallback {
                override fun onLoginSuccess() {
                    setProgressIndicator(false)
                    this@LoginPresenter.onLoginSuccess()
                }

                override fun onLoginFailure(message: String?) {
                    setProgressIndicator(false)
                    this@LoginPresenter.onLoginFailure(message)
                }
            })
        } else {
            view?.showNoInternetMessage()
        }
    }

    override fun onPrivacyPolicyObtained(privacyPolicy: String) {
        if (!isViewAvailable) return
        setProgressIndicator(false)

        if (privacyPolicy != null) {
            view?.showPrivacyPolicyDialog(privacyPolicy)
        } else {
            view?.showErrorMessage(PawApplication.getContext().getString(R.string.txt_error_getting_privacy_policy))
        }
    }

    override fun onUserAcceptedPrivacyPolicy() {
        setProgressIndicator(true)
        userManager.setHasAcceptedPrivacyPolicy(true, object : UserManager.SetUserPropertyCallback {
            override fun onSuccess() {
                if (!isViewAvailable) return
                view?.closeLoginScreen()
            }

            override fun onFailure(message: String?) {
                if (!isViewAvailable) return
                setProgressIndicator(false)
                view?.showErrorMessage(message)
            }
        })
    }

    override fun onUserDeclinedPrivacyPolicy() {
        setProgressIndicator(true)
        userManager.logout(object : UserManager.LogoutCallback {
            override fun onLogoutSuccess() {
                setProgressIndicator(false)
            }

            override fun onLogoutFailure(message: String) {
                message?.let {
                    setProgressIndicator(false)
                }
            }
        })
    }

    companion object {
        private val MIN_PASS_LENGTH = 6
    }
}
