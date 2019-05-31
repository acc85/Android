package org.helpapaw.helpapaw.authentication.register

import android.app.AlertDialog
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.databinding.FragmentRegisterBinding
import org.helpapaw.helpapaw.reusable.AlertDialogFragment
import org.helpapaw.helpapaw.viewmodels.RegisterResult
import org.helpapaw.helpapaw.viewmodels.RegisterViewModel
import org.koin.android.ext.android.inject


class RegisterFragment : BaseFragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val registerViewModel: RegisterViewModel by inject()
    private val whyPhoneDialogFragment: WhyPhoneDialogFragment by inject()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)
        binding.viewModel = registerViewModel

        registerViewModel.registerLiveData.observe(this, Observer { registerResult ->
            when (registerResult) {
                is RegisterResult.ShowPrivacyDialog -> {
                    registerViewModel.registerLiveData.value = null
                    val builder = AlertDialog.Builder(activity)
                    builder.setMessage(Html.fromHtml(registerResult.messaage))
                            .setPositiveButton(R.string.accept) { _, _ -> registerViewModel.attemptToRegister() }
                            .setNegativeButton(R.string.decline) { _, _ -> registerViewModel.showProgress(false) }
                            .setCancelable(false)
                            .show()
                }
                is RegisterResult.Success -> {
                    registerViewModel.registerLiveData.value = null
                    AlertDialogFragment.showAlert(getString(R.string.txt_success), getString(R.string.txt_registration_successful), false, this.fragmentManager)
                    activity?.supportFragmentManager?.popBackStack()
                }
                is RegisterResult.ShowWhyDialog -> {
                    registerViewModel.registerLiveData.value = null
                    activity?.let{act->
                        whyPhoneDialogFragment.show(act.supportFragmentManager, whyPhoneDialogFragment.tag)
                    }
                }
                is RegisterResult.ShowNoInternetMessage -> {
                    registerViewModel.registerLiveData.value = null

                    showErrorMessage(getString(R.string.txt_no_internet))
                }
                is RegisterResult.CloseScreen -> {
                    registerViewModel.registerLiveData.value = null
                    activity?.supportFragmentManager?.popBackStack()
                }
            } })

        return binding.root
    }

    private fun showErrorMessage(message: String?) {
        AlertDialogFragment.showAlert(getString(R.string.txt_error), message, true, this.fragmentManager)
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
    }

    companion object {

        fun newInstance(): RegisterFragment {
            return RegisterFragment()
        }
    }
}
