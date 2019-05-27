package org.helpapaw.helpapaw.authentication.login

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.authentication.AuthenticationActivity
import org.helpapaw.helpapaw.authentication.AuthenticationFragment
import org.helpapaw.helpapaw.authentication.register.RegisterFragment
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.base.PresenterManager
import org.helpapaw.helpapaw.databinding.FragmentLoginBinding
import org.helpapaw.helpapaw.reusable.AlertDialogFragment
import org.helpapaw.helpapaw.signalsmap.SignalsMapActivity
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import java.util.*


class LoginFragment : AuthenticationFragment(), LoginContract.View {

    val loginPresenter: LoginPresenter by inject{ parametersOf(this)}
    lateinit var actionsListener: LoginContract.UserActionsListener

    lateinit var binding: FragmentLoginBinding

    /* OnClick Listeners */

    val btnLoginClickListener: View.OnClickListener
        get() = View.OnClickListener {
            val email = binding.editEmail.text.toString().trim { it <= ' ' }
            val password = binding.editPassword.text.toString()
            actionsListener.onLoginButtonClicked(email, password)
        }

    val btnShowRegisterClickListener: View.OnClickListener
        get() = View.OnClickListener { actionsListener.onRegisterButtonClicked() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

//        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter<Presenter>(getScreenId()) == null) {
//            loginPresenter = LoginPresenter(this)
//        } else {
//            loginPresenter = PresenterManager.getInstance().getPresenter(getScreenId())
//            loginPresenter.view = this
//        }

        actionsListener = loginPresenter
        ppResponseListener = loginPresenter

        binding.btnLogin.setOnClickListener(btnLoginClickListener)
        binding.btnShowRegister.setOnClickListener(btnShowRegisterClickListener)

        binding.btnLoginFb.setReadPermissions(Arrays.asList("email"))
        // Callback registration
        val activity = activity as AuthenticationActivity?
        binding.btnLoginFb.registerCallback(activity!!.callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                actionsListener.onLoginFbSuccess(loginResult.accessToken.token)
            }

            override fun onCancel() {
                // Do nothing
            }

            override fun onError(exception: FacebookException) {
                // App code
                showErrorMessage(exception.message)
            }
        })

        actionsListener.onInitLoginScreen()

        return binding.root
    }

    override fun showErrorMessage(message: String?) {
        AlertDialogFragment.showAlert("Error", message, true, this.fragmentManager)
    }

    override fun showEmailErrorMessage() {
        binding.editEmail.error = getString(R.string.txt_invalid_email)
    }

    override fun showPasswordErrorMessage() {
        binding.editPassword.error = getString(R.string.txt_invalid_password)
    }

    override fun clearErrorMessages() {
        binding.editEmail.error = null
        binding.editPassword.error = null
    }

    override fun openRegisterScreen() {
        val registerFragment = RegisterFragment.newInstance()
        openFragment(registerFragment, true, true, true)
    }

    override fun setProgressIndicator(active: Boolean) {
        binding.progressLogin.visibility = if (active) View.VISIBLE else View.GONE
        binding.grpLogin.visibility = if (active) View.GONE else View.VISIBLE
    }

    override fun getPresenter(): Presenter<*> {
        return loginPresenter
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
    }

    override fun closeLoginScreen() {
        if (activity != null) {
            Toast.makeText(activity, R.string.txt_login_successful, Toast.LENGTH_LONG).show()
            val intent = Intent(context, SignalsMapActivity::class.java)
            startActivity(intent)
            activity!!.finish()
        }
    }

    override fun showNoInternetMessage() {
        showErrorMessage(getString(R.string.txt_no_internet))
    }

    override fun isActive(): Boolean {
        return isAdded
    }

    companion object {

        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}// Required empty public constructor