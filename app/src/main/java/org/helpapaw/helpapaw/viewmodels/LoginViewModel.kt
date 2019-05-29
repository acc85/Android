package org.helpapaw.helpapaw.viewmodels

import android.text.TextUtils.isEmpty
import android.view.View
import androidx.databinding.Bindable
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.runBlocking
import org.helpapaw.helpapaw.BR
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.user.UserManager
import org.helpapaw.helpapaw.utils.Utils
import java.util.*

sealed class HelpAPawLoginResult {
    data class Success(var empty: String = "") : HelpAPawLoginResult()
    data class Fail(var exception: String?) : HelpAPawLoginResult()
    data class ShowPrivacyDialog(var privacyData: String?) : HelpAPawLoginResult()
    data class ShowNoInternetMessage(val nothing: String = "") : HelpAPawLoginResult()
}

const val MIN_PASS_LENGTH = 6

class LoginViewModel(
        val userManager: UserManager,
        val utils: Utils,
        val callbackManager: CallbackManager,
        val authenticationLiveData: MutableLiveData<HelpAPawLoginResult> = MutableLiveData()
) : BaseViewModel() {

    @Bindable
    var username: String = ""

    @Bindable
    var password: String = ""

    @Bindable
    var userNameErrorText: String? = null
        set(value) {
            field = value
            notifyChange(BR.userNameErrorText)
        }

    @Bindable
    var passwordErrorText: String? = null
        set(value) {
            field = value
            notifyChange(BR.passwordErrorText)
        }

    @Bindable
    var showProgress: Int = View.GONE
        set(value) {
            field = value
            notifyChange(BR.showProgress)
        }

    @Bindable
    var showGroupLogin: Int = View.VISIBLE
        set(value) {
            field = value
            notifyChange(BR.showGroupLogin)
        }


    fun onUserAcceptedPrivacyPolicy() {
        showProgress = View.VISIBLE
        showGroupLogin = View.GONE
        userManager.setHasAcceptedPrivacyPolicy(true, object : UserManager.UserPropertyCallback {
            override fun onResult(userPropertyResult: UserManager.UserPropertyResult) {
                when (userPropertyResult) {
                    is UserManager.UserPropertyResult.Success -> {
                        authenticationLiveData.value = HelpAPawLoginResult.Success("")
                    }
                    is UserManager.UserPropertyResult.Failed -> {
                        showProgress = View.GONE
                        showGroupLogin = View.VISIBLE
                        authenticationLiveData.value = HelpAPawLoginResult.Fail(userPropertyResult.message)
                    }
                }
            }
        })
    }

    fun onUserDeclinedPrivacyPolicy() {
        showProgress = View.VISIBLE
        showGroupLogin = View.GONE
        userManager.logout(object : UserManager.LogoutCallback {
            override fun onLogoutSuccess() {
                showProgress = View.GONE
                showGroupLogin = View.VISIBLE
            }

            override fun onLogoutFailure(message: String) {
                showProgress = View.GONE
                showGroupLogin = View.VISIBLE
            }
        })
    }

    fun verify(view:View):Boolean{
        var isValid = true
        if (isEmpty(username) || !utils.isEmailValid(username)) {
            userNameErrorText = view.context.getString(R.string.txt_invalid_email)
            isValid = false
        }

        if (isEmpty(username) || !utils.isEmailValid(username)) {
            userNameErrorText = view.context.getString(R.string.txt_invalid_email)
            isValid = false
        }

        if (isEmpty(password) || password.length < MIN_PASS_LENGTH) {
            passwordErrorText = view.context.getString(R.string.txt_invalid_password)
            isValid = false
        }
        return isValid
    }

    fun attemptToLogin(view: View) {

        userNameErrorText = null
        passwordErrorText = null
        if(verify(view)) {
            if (utils.hasNetworkConnection()) {
                showProgress = View.VISIBLE
                showGroupLogin = View.GONE
                userManager.login(username, password, object : UserManager.LoginCallback {
                    override fun onLoginSuccess() {
                        runBlocking {
                            getPrivacyPolicyDetials(view)
                        }
                    }

                    override fun onLoginFailure(message: String?) {
                        showProgress = View.GONE
                        showGroupLogin = View.VISIBLE
                        authenticationLiveData.value = HelpAPawLoginResult.Fail(message)
                    }
                })
            } else {
                authenticationLiveData.value = HelpAPawLoginResult.ShowNoInternetMessage("")
                showProgress = View.GONE
                showGroupLogin = View.VISIBLE
            }
        }
    }

    fun getPrivacyPolicyDetials(view: View) {
        userManager.getHasAcceptedPrivacyPolicy(object : UserManager.UserPropertyCallback {
            override fun onResult(userPropertyResult: UserManager.UserPropertyResult) {
                when (userPropertyResult) {
                    is UserManager.UserPropertyResult.Success -> {
                        try {
                            val accepted = userPropertyResult.value as Boolean
                            if (!accepted) {
                                runBlocking {
                                    val result: String? = Utils.getHtmlByCouroutines(view.context.getString(R.string.url_privacy_policy)).await()
                                    showProgress = View.GONE
                                    showGroupLogin = View.VISIBLE
                                    if (result != null) {
                                        authenticationLiveData.value = HelpAPawLoginResult.ShowPrivacyDialog(result)
                                    } else {
                                        authenticationLiveData.value = HelpAPawLoginResult.Fail(view.context.getString(R.string.txt_error_getting_privacy_policy))
                                    }
                                }
                            } else {
                                authenticationLiveData.value = HelpAPawLoginResult.Success("")
                            }
                        } catch (ignored: Exception) {
                            authenticationLiveData.value = HelpAPawLoginResult.Success("")
                        }
                    }
                    is UserManager.UserPropertyResult.Failed -> {
                        showProgress = View.VISIBLE
                        showGroupLogin = View.GONE
                        authenticationLiveData.value = HelpAPawLoginResult.Fail(userPropertyResult.message)
                    }
                }
            }
        })
    }

}

@BindingAdapter(value = ["facebookLoginSetup"])
fun setUpfaceBookLogin(view: LoginButton, viewModel: LoginViewModel) {
    view.setPermissions(Arrays.asList("email"))
    view.registerCallback(viewModel.callbackManager, object : FacebookCallback<LoginResult> {
        override fun onSuccess(loginResult: LoginResult) {
            viewModel.showProgress = View.VISIBLE
            viewModel.showGroupLogin = View.GONE
            viewModel.userManager.loginWithFacebook(loginResult.accessToken.token, object : UserManager.LoginCallback {
                override fun onLoginSuccess() {
                    runBlocking {
                        viewModel.getPrivacyPolicyDetials(view)
                    }
                }

                override fun onLoginFailure(message: String?) {
                    viewModel.showProgress = View.GONE
                    viewModel.showGroupLogin = View.VISIBLE
                    viewModel.authenticationLiveData.value = HelpAPawLoginResult.Fail(message)
                }
            })


        }

        override fun onCancel() {
            // Do nothing
        }

        override fun onError(exception: FacebookException) {
            viewModel.authenticationLiveData.value = HelpAPawLoginResult.Fail(exception.message)
        }
    })
}

@BindingAdapter(value = ["userError"])
fun setUsernameErrorMessage(view: TextInputEditText, errorText: String?) {
    view.error = errorText
}


@BindingAdapter(value = ["passwordError"])
fun setPasswordErrorMessage(view: TextInputEditText, errorText: String?) {
    view.error = errorText
}
