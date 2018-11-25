package org.helpapaw.helpapaw.authentication.register

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import dagger.android.support.AndroidSupportInjection
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.databinding.FragmentRegisterBinding
import org.helpapaw.helpapaw.reusable.AlertDialogFragment
import javax.inject.Inject

class RegisterFragment:BaseFragment(), RegisterContract.View{

    @Inject
    lateinit var registerPresenter: RegisterPresenter

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
    }

    override fun showMessage(message: String) {
        AlertDialogFragment.showAlert("Error", message, true, this.fragmentManager)
    }

    override fun showEmailErrorMessage() {
        binding.editEmail.error = getString(R.string.txt_invalid_email)
    }

    override fun showPasswordErrorMessage() {
        binding.editPassword.error = getString(R.string.txt_invalid_password)
    }

    override fun isActive(): Boolean {
        return isAdded
    }

    override fun getPresenter(): Presenter<*>? {
        return registerPresenter
    }

    private var actionsListener: RegisterContract.UserActionsListener? = null

    internal lateinit var binding: FragmentRegisterBinding

    companion object {
        fun newInstance():RegisterFragment{
            return RegisterFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)

        actionsListener = registerPresenter

        binding.btnSignup.setOnClickListener(getBtnSignUpListener())
        binding.btnShowLogin.setOnClickListener(getBtnShowLoginClickListener())
        binding.txtWhyPhone.setOnClickListener(getTxtWhyPhoneClickListener())

        actionsListener?.onInitRegisterScreen()
        return binding.root
    }

    override fun showPasswordConfirmationErrorMessage() {
        binding.editPasswordConfirmation.error = getString(R.string.txt_invalid_password_confirmation)
    }

    override fun showNameErrorMessage() {
        binding.editName.error = getString(R.string.txt_name_required)
    }

    override fun showWhyPhoneDialog() {
        val whyPhoneDialogFragment = WhyPhoneDialogFragment.newInstance()
        whyPhoneDialogFragment.show(activity?.supportFragmentManager, whyPhoneDialogFragment.tag)
    }

    override fun clearErrorMessages() {
        binding.editEmail.error = null
        binding.editPassword.error = null
    }

    override fun setProgressIndicator(active: Boolean) {
        binding.progressRegister.visibility = if (active) View.VISIBLE else View.GONE
        binding.grpRegister.visibility = if (active) View.GONE else View.VISIBLE
    }

    override fun closeRegistrationScreen() {
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun showNoInternetMessage() {
        showMessage(getString(R.string.txt_no_internet))
    }

    private fun getBtnShowLoginClickListener(): View.OnClickListener {
        return View.OnClickListener { actionsListener?.onLoginButtonClicked() }
    }

    private fun getBtnSignUpListener(): View.OnClickListener {
        return View.OnClickListener {
            val email = binding.editEmail.text?.toString()?.trim()?:""
            val password = binding.editPassword.text?.toString()?:""
            val passwordConfirmation = binding.editPasswordConfirmation.text?.toString()?:""
            val name = binding.editName.text?.toString()?:""
            val phoneNumber = binding.editPhone.text?.toString()?:""

            actionsListener?.onRegisterButtonClicked(email, password, passwordConfirmation, name, phoneNumber)
        }
    }

    private fun getTxtWhyPhoneClickListener(): View.OnClickListener {
        return View.OnClickListener { actionsListener?.onWhyPhoneButtonClicked() }
    }

}