package org.helpapaw.helpapaw.authentication.login

import androidx.appcompat.app.AppCompatActivity
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.facebook.CallbackManager
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.data.user.UserManager
import org.helpapaw.helpapaw.utils.Injection
import org.helpapaw.helpapaw.utils.Utils

class LoginPresenter(val view: LoginContract.View): Presenter<LoginContract.View>(view), LoginContract.UserActionsListener{

    companion object {
        private const val MIN_PASS_LENGTH:Int = 6

    }

    private val userManager:UserManager = Injection.getUserManagerInstance()
    private var showProgressBar:Boolean = false
    private val facebookLogin = Injection.getFacebookLogin()

    override fun onInitLoginScreen() {
        setProgressIndicator(showProgressBar)
    }

    override fun onLoginButtonClicked(email: String, password: String) {
        getView().clearErrorMessages()

        if (email.isBlank() || !Utils.getInstance().isEmailValid(email)) {
            getView().showEmailErrorMessage()
            return
        }

        if (password.isEmpty() || password.length < MIN_PASS_LENGTH) {
            getView().showPasswordErrorMessage()
            return
        }

        getView().hideKeyboard()
        setProgressIndicator(true)
        attemptToLogin(email, password)

    }

    override fun onRegisterButtonClicked() {
        getView().openRegisterScreen()
    }

    override fun onLoginFbButtonClicked(activity: AppCompatActivity, callbackManager: CallbackManager) {
        setProgressIndicator(true)
        facebookLogin.loginWithFacebook(activity,
                callbackManager,
                object : AsyncCallback<BackendlessUser> {
                    override fun handleResponse(loggedInUser: BackendlessUser ) {
                        // user logged in successfully
                        onLoginSuccess()
                    }
                    @Override
                    override fun handleFault(fault:BackendlessFault) {
                        // failed to log in
                        onLoginFailure(fault.getMessage())
                    }
                })
    }

    private fun attemptToLogin(email:String , password:String ) {
        if (Utils.getInstance().hasNetworkConnection()) {
            userManager.login(email, password, object : UserManager.LoginCallback {
                override fun onLoginSuccess() {
                    this.onLoginSuccess()
                }

                override fun onLoginFailure(message:String ) {
                    this.onLoginFailure(message)
                }
            })
        } else {
            getView().showNoInternetMessage()
            setProgressIndicator(false)
        }
    }

    private fun onLoginSuccess() {
        if (!isViewAvailable()) return
        getView().closeLoginScreen()
    }

    private fun onLoginFailure(message:String) {
        if (!isViewAvailable()) return
        setProgressIndicator(false)
        getView().showMessage(message)
    }

    private fun setProgressIndicator(active:Boolean) {
        getView().setProgressIndicator(active)
        showProgressBar = active
    }

    private fun isViewAvailable():Boolean {
        return getView() != null && getView().isActive()
    }

}