package org.helpapaw.helpapaw.authentication.login

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.authentication.register.RegisterFragment
import org.helpapaw.helpapaw.base.BaseFragment
import org.helpapaw.helpapaw.databinding.FragmentLoginBinding
import org.helpapaw.helpapaw.reusable.AlertDialogFragment
import org.helpapaw.helpapaw.signalsmap.SignalsMapActivity
import org.helpapaw.helpapaw.viewmodels.HelpAPawLoginResult
import org.helpapaw.helpapaw.viewmodels.LoginViewModel
import org.koin.android.ext.android.inject


class LoginFragment : BaseFragment(), LoginContract.View {

    val viewModel: LoginViewModel by inject()

    lateinit var binding: FragmentLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.viewModel = viewModel

        binding.btnShowRegister.setOnClickListener{
            val registerFragment = RegisterFragment.newInstance()
            openFragment(registerFragment, true, true, true)
        }

        viewModel.authenticationLiveData.observe(this, Observer<HelpAPawLoginResult>{ result->
            when(result){
                is HelpAPawLoginResult.Success->{
                    activity?.let{act->
                        Toast.makeText(act, R.string.txt_login_successful, Toast.LENGTH_LONG).show()
                        val intent = Intent(context, SignalsMapActivity::class.java)
                        startActivity(intent)
                        act.finish()
                    }
                }
                is HelpAPawLoginResult.ShowPrivacyDialog->{
                    val builder = AlertDialog.Builder(activity)
                    builder.setMessage(Html.fromHtml(result.privacyData))
                            .setPositiveButton(R.string.accept) { dialogInterface, i -> viewModel.onUserAcceptedPrivacyPolicy() }
                            .setNegativeButton(R.string.decline) { dialogInterface, i -> viewModel.onUserDeclinedPrivacyPolicy() }
                            .setCancelable(false)
                            .show()
                }
                is HelpAPawLoginResult.Fail->{
                    AlertDialogFragment.showAlert("Error", result.exception, true, this.fragmentManager)
                }
            }
        })

        return binding.root
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
    }

    companion object {

        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}// Required empty public constructor