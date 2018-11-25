package org.helpapaw.helpapaw.authentication

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.facebook.CallbackManager
import dagger.android.AndroidInjection
import dagger.android.support.DaggerAppCompatActivity
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.authentication.login.LoginFragment
import org.helpapaw.helpapaw.databinding.ActivityAuthenticationBinding
import javax.inject.Inject

class AuthenticationActivity: DaggerAppCompatActivity(){

    internal lateinit var binding: ActivityAuthenticationBinding

    @Inject
    lateinit var callbackManager: CallbackManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        if (null == savedInstanceState) {
            initFragment(LoginFragment.newInstance())
        }
    }

    private fun initFragment(loginFragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.grp_content_frame, loginFragment)
        transaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}