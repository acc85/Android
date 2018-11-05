package org.helpapaw.helpapaw.authentication.login

import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager

interface LoginContract{

    interface View{

        fun showMessage(message:String)

        fun showEmailErrorMessage()

        fun showPasswordErrorMessage()

        fun clearErrorMessages()

        fun openRegisterScreen()

        fun setProgressIndicator(active:Boolean)

        fun hideKeyboard()

        fun closeLoginScreen()

        fun showNoInternetMessage()

        fun isActive():Boolean
    }

    interface UserActionsListener {

        fun onInitLoginScreen()

        fun onLoginButtonClicked(email:String, password:String)

        fun onRegisterButtonClicked()

        fun onLoginFbButtonClicked(activity: AppCompatActivity, callbackManager: CallbackManager)
    }

}