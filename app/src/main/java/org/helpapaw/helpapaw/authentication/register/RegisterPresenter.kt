package org.helpapaw.helpapaw.authentication.register

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
class RegisterPresenter(
        view: RegisterContract.View,
        val userManager:UserManager,
        val utils:Utils

) : Presenter<RegisterContract.View>(view), RegisterContract.UserActionsListener, PrivacyPolicyConfirmationContract.Obtain, PrivacyPolicyConfirmationContract.UserResponse {

    private var showProgressBar: Boolean = false

    private var email: String? = null
    private var password: String? = null
    private var name: String? = null
    private var phoneNumber: String? = null

    private val isViewAvailable: Boolean
        get() = view != null && view!!.isActive()

    init {
        showProgressBar = false
    }

    override fun onInitRegisterScreen() {
        setProgressIndicator(showProgressBar)
    }

    override fun onRegisterButtonClicked(email: String, password: String, passwordConfirmation: String, name: String, phoneNumber: String) {
        view?.clearErrorMessages()

        if (isEmpty(email) || !utils.isEmailValid(email)) {
            view?.showEmailErrorMessage()
            return
        }

        if (isEmpty(password) || password.length < MIN_PASS_LENGTH) {
            view?.showPasswordErrorMessage()
            return
        }

        if (password != passwordConfirmation) {
            view?.showPasswordConfirmationErrorMessage()
            return
        }

        if (isEmpty(name)) {
            view?.showNameErrorMessage()
            return
        }

        view?.hideKeyboard()
        setProgressIndicator(true)

        // Save values for later;
        this.email = email
        this.password = password
        this.name = name
        this.phoneNumber = phoneNumber

        val privacyPolicyConfirmationGetter = PrivacyPolicyConfirmationGetter(this, PawApplication.getContext())
        privacyPolicyConfirmationGetter.execute()
    }

    private fun attemptToRegister(email: String?, password: String?, name: String?, phoneNumber: String?) {
        if (utils.hasNetworkConnection()) {
            userManager.register(email!!, password!!, name!!, phoneNumber!!, object : UserManager.RegistrationCallback {
                override fun onRegistrationSuccess() {
                    if (!isViewAvailable) return
                    view?.showRegistrationSuccessfulMessage()
                    view?.closeRegistrationScreen()
                }

                override fun onRegistrationFailure(message: String?) {
                    if (!isViewAvailable) return
                    setProgressIndicator(false)
                    view?.showErrorMessage(message)
                }
            })
        } else {
            view?.showNoInternetMessage()
            setProgressIndicator(false)
        }
    }

    private fun setProgressIndicator(active: Boolean) {
        view?.setProgressIndicator(active)
        this.showProgressBar = active
    }

    override fun onLoginButtonClicked() {
        view?.closeRegistrationScreen()
    }

    override fun onWhyPhoneButtonClicked() {
        view?.showWhyPhoneDialog()
    }

    private fun isEmpty(value: String?): Boolean {
        return !(value != null && value.length > 0)
    }

    override fun onPrivacyPolicyObtained(privacyPolicy: String) {
        if (!isViewAvailable) return

        if (privacyPolicy != null) {
            view?.showPrivacyPolicyDialog(privacyPolicy)
        } else {
            setProgressIndicator(false)
            view?.showErrorMessage(PawApplication.getContext().getString(R.string.txt_error_getting_privacy_policy))
        }
    }

    override fun onUserAcceptedPrivacyPolicy() {
        attemptToRegister(email, password, name, phoneNumber)
    }

    override fun onUserDeclinedPrivacyPolicy() {
        email = null
        password = null
        name = null
        phoneNumber = null

        setProgressIndicator(false)
    }

    companion object {

        private val MIN_PASS_LENGTH = 6
    }
}
