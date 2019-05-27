package org.helpapaw.helpapaw.authentication.register

/**
 * Created by iliyan on 7/25/16
 */
interface RegisterContract {

    interface View {

        fun isActive(): Boolean

        fun showErrorMessage(message: String?)

        fun showEmailErrorMessage()

        fun showPasswordErrorMessage()

        fun showPasswordConfirmationErrorMessage()

        fun showNameErrorMessage()

        fun showWhyPhoneDialog()

        fun showPrivacyPolicyDialog(privacyPolicy: String)

        fun clearErrorMessages()

        fun hideKeyboard()

        fun setProgressIndicator(active: Boolean)

        fun showRegistrationSuccessfulMessage()

        fun closeRegistrationScreen()

        fun showNoInternetMessage()
    }

    interface UserActionsListener {

        fun onInitRegisterScreen()

        fun onRegisterButtonClicked(email: String, password: String, passwordConfirmation: String, name: String, phoneNumber: String)

        fun onLoginButtonClicked()

        fun onWhyPhoneButtonClicked()

    }
}
