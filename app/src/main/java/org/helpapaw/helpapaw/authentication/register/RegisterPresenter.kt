package org.helpapaw.helpapaw.authentication.register

import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.data.user.BackendlessUserManager
import org.helpapaw.helpapaw.data.user.UserManager
import org.helpapaw.helpapaw.utils.Utils
import javax.inject.Inject

class RegisterPresenter(override var view: RegisterContract.View?): Presenter<RegisterContract.View>(view),RegisterContract.UserActionsListener {

    @Inject
    constructor(view:RegisterFragment):this(view as RegisterContract.View)

    @Inject
    lateinit var utils:Utils

    companion object {
        private const val MIN_PASS_LENGTH = 6
    }

    @Inject
    lateinit var userManager: BackendlessUserManager

    private var showProgressBar: Boolean = false


    override fun onInitRegisterScreen() {
        setProgressIndicator(showProgressBar)
    }

    override fun onRegisterButtonClicked(email: String, password: String, passwordConfirmation: String, name: String, phoneNumber: String) {
        view?.clearErrorMessages()

        if (email.isEmpty() || !utils.isEmailValid(email)) {
            view?.showEmailErrorMessage()
            return
        }

        if (password.isEmpty() || password.length < MIN_PASS_LENGTH) {
            view?.showPasswordErrorMessage()
            return
        }

        if (password != passwordConfirmation) {
            view?.showPasswordConfirmationErrorMessage()
            return
        }

        if (name.isEmpty()) {
            view?.showNameErrorMessage()
            return
        }

        view?.hideKeyboard()
        setProgressIndicator(true)
        attemptToRegister(email, password, name, phoneNumber)
    }

    override fun onLoginButtonClicked() {
        view?.closeRegistrationScreen()
    }

    override fun onWhyPhoneButtonClicked() {
        view?.showWhyPhoneDialog()
    }

    private fun attemptToRegister(email: String, password: String, name: String, phoneNumber: String) {
        if (utils.hasNetworkConnection()) {
            userManager.register(email, password, name, phoneNumber, object : UserManager.RegistrationCallback {
                override fun onRegistrationSuccess() {
                    if (!view!!.isActive()) return
                    view?.closeRegistrationScreen()
                }

                override fun onRegistrationFailure(message: String) {
                    if (!view!!.isActive()) return
                    setProgressIndicator(false)
                    view?.showMessage(message)
                }
            })
        } else {
            view?.showNoInternetMessage()
            setProgressIndicator(false)
        }
    }

    private fun setProgressIndicator(active: Boolean) {
        view?.setProgressIndicator(active)
        showProgressBar = active
    }


}