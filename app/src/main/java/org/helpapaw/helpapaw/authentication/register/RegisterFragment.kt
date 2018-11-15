package org.helpapaw.helpapaw.authentication.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.base.Presenter
import org.helpapaw.helpapaw.base.PresenterManager
import org.helpapaw.helpapaw.databinding.FragmentRegisterBinding
import org.helpapaw.helpapaw.reusable.AlertDialogFragment

class RegisterFragment:BaseFragment (), RegisterContract.View{

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

    private var registerPresenter: RegisterPresenter? = null
    private var actionsListener: RegisterContract.UserActionsListener? = null

    internal lateinit var binding: FragmentRegisterBinding

    companion object {
        fun newInstance():RegisterFragment{
            return RegisterFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)

        if (savedInstanceState == null || PresenterManager.instance.getPresenter<RegisterPresenter>(screenId) == null) {
            registerPresenter = RegisterPresenter(this)
        } else {
            registerPresenter = PresenterManager.instance.getPresenter(screenId)
            registerPresenter?.view = this
        }

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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