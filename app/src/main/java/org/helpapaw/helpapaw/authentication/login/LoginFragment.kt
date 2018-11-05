package org.helpapaw.helpapaw.authentication.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.authentication.AuthenticationActivity
import org.helpapaw.helpapaw.authentication.register.RegisterFragment
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.base.PresenterManager
import org.helpapaw.helpapaw.databinding.FragmentLoginBinding
import org.helpapaw.helpapaw.reusable.AlertDialogFragment


class LoginFragment : BaseFragment(), LoginContract.View {
    override fun showMessage(message: String) {
        AlertDialogFragment.showAlert("Error", message, true, fragmentManager)
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

    override fun hideKeyboard() {
        super.hideKeyboard()
    }

    override fun closeLoginScreen() {
        activity?.finish()
    }

    override fun showNoInternetMessage() {
        showMessage(getString(R.string.txt_no_internet))
    }

    override fun isActive(): Boolean {
        return isAdded
    }

    override fun getPresenter(): Presenter<*> {
        return loginPresenter
    }

    lateinit var loginPresenter: LoginPresenter
    lateinit var actionsListener: LoginContract.UserActionsListener
    lateinit var binding: FragmentLoginBinding


    companion object {
        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_login, container, false)
        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter<LoginPresenter>(screenId) == null) {
            loginPresenter = LoginPresenter (this)
        } else {
            loginPresenter = PresenterManager.getInstance().getPresenter(screenId)
            loginPresenter.setView(this)
        }

        return super.onCreateView(inflater, container, savedInstanceState)

    }


    fun getBtnLoginClickListener():View.OnClickListener{
        return View.OnClickListener {
            val email:String  = binding.editEmail.text.toString().trim()
            val password:String  = binding.editPassword.text.toString()
            actionsListener.onLoginButtonClicked(email, password)
        }
    }


    fun getBtnShowRegisterClickListener(): View.OnClickListener {
        return View.OnClickListener {
            actionsListener.onRegisterButtonClicked()
        }
    }

    fun getBtnLoginFbClickListener():View.OnClickListener{
        return View.OnClickListener {
            val activity:AuthenticationActivity  = this.activity as AuthenticationActivity
            actionsListener.onLoginFbButtonClicked(activity, activity.callbackManager)
        }

    }
}