package org.helpapaw.helpapaw.authentication.register

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.authentication.AuthenticationFragment
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.databinding.FragmentRegisterBinding
import org.helpapaw.helpapaw.reusable.AlertDialogFragment
import org.helpapaw.helpapaw.viewmodels.RegisterViewModel
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf


class RegisterFragment : AuthenticationFragment(), RegisterContract.View {

    private val registerPresenter: RegisterPresenter by inject{ parametersOf(this)}
    private lateinit var actionsListener: RegisterContract.UserActionsListener

    private lateinit var binding: FragmentRegisterBinding

    /* OnClick Listeners */

    val btnShowLoginClickListener: View.OnClickListener
        get() = View.OnClickListener { actionsListener.onLoginButtonClicked() }

    val btnSignUpListener: View.OnClickListener
        get() = View.OnClickListener {
            val email = binding.editEmail.text.toString().trim { it <= ' ' }
            val password = binding.editPassword.text.toString()
            val passwordConfirmation = binding.editPasswordConfirmation.text.toString()
            val name = binding.editName.text.toString()
            val phoneNumber = binding.editPhone.text.toString()

            actionsListener.onRegisterButtonClicked(email, password, passwordConfirmation, name, phoneNumber)
        }

    val txtWhyPhoneClickListener: View.OnClickListener
        get() = View.OnClickListener { actionsListener.onWhyPhoneButtonClicked() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        binding.viewModel = ViewModelProviders.of(this).get(RegisterViewModel::class.java)
        if (savedInstanceState != null){
            registerPresenter.view = this
        }

        actionsListener = registerPresenter
        ppResponseListener = registerPresenter

        binding.btnSignup.setOnClickListener(btnSignUpListener)
        binding.btnShowLogin.setOnClickListener(btnShowLoginClickListener)
        binding.txtWhyPhone.setOnClickListener(txtWhyPhoneClickListener)

        actionsListener.onInitRegisterScreen()

        return binding.root
    }

    override fun showErrorMessage(message: String?) {
        AlertDialogFragment.showAlert(getString(R.string.txt_error), message, true, this.fragmentManager)
    }

    override fun showEmailErrorMessage() {
        binding.editEmail.error = getString(R.string.txt_invalid_email)
    }

    override fun showPasswordErrorMessage() {
        binding.editPassword.error = getString(R.string.txt_invalid_password)
    }

    override fun showPasswordConfirmationErrorMessage() {
        binding.editPasswordConfirmation.error = getString(R.string.txt_invalid_password_confirmation)
    }

    override fun showNameErrorMessage() {
        binding.editName.error = getString(R.string.txt_name_required)
    }

    override fun showWhyPhoneDialog() {
        val whyPhoneDialogFragment = WhyPhoneDialogFragment.newInstance()
        whyPhoneDialogFragment.show(activity!!.fragmentManager, whyPhoneDialogFragment.tag)
    }

    override fun clearErrorMessages() {
        binding.editEmail.error = null
        binding.editPassword.error = null
    }

    override fun showRegistrationSuccessfulMessage() {
        AlertDialogFragment.showAlert(getString(R.string.txt_success), getString(R.string.txt_registration_successful), false, this.fragmentManager)
    }

    override fun closeRegistrationScreen() {
        if (activity != null) {
            activity!!.supportFragmentManager.popBackStack()
        }
    }

    override fun showNoInternetMessage() {
        showErrorMessage(getString(R.string.txt_no_internet))
    }

    override fun isActive(): Boolean {
        return isAdded
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
    }

    override fun setProgressIndicator(active: Boolean) {
        binding.progressRegister.visibility = if (active) View.VISIBLE else View.GONE
        binding.grpRegister.visibility = if (active) View.GONE else View.VISIBLE
    }

//    override fun getPresenter(): Presenter<*> {
//        return registerPresenter
//    }

    companion object {

        fun newInstance(): RegisterFragment {
            return RegisterFragment()
        }
    }
}// Required empty public constructor
