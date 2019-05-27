package org.helpapaw.helpapaw.authentication.login

/**
 * Created by iliyan on 7/25/16
 */
interface LoginContract {

    interface View {

        fun isActive(): Boolean

        fun showErrorMessage(message: String?)

        fun showEmailErrorMessage()

        fun showPasswordErrorMessage()

        fun clearErrorMessages()

        fun openRegisterScreen()

        fun setProgressIndicator(active: Boolean)

        fun hideKeyboard()

        fun showPrivacyPolicyDialog(privacyPolicy: String)

        fun closeLoginScreen()

        fun showNoInternetMessage()
    }

    interface UserActionsListener {

        fun onInitLoginScreen()

        fun onLoginButtonClicked(email: String, password: String)

        fun onRegisterButtonClicked()

        fun onLoginFbSuccess(accessToken: String)
    }
}
