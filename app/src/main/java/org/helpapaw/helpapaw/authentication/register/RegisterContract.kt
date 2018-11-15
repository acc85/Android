package org.helpapaw.helpapaw.authentication.register

interface RegisterContract{

    interface View {

        fun isActive(): Boolean

        fun showMessage(message: String)

        fun showEmailErrorMessage()

        fun showPasswordErrorMessage()

        fun showPasswordConfirmationErrorMessage()

        fun showNameErrorMessage()

        fun showWhyPhoneDialog()

        fun clearErrorMessages()

        fun hideKeyboard()

        fun setProgressIndicator(active: Boolean)

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