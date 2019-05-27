package org.helpapaw.helpapaw.authentication

import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity

import com.facebook.CallbackManager

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.authentication.login.LoginFragment
import org.helpapaw.helpapaw.databinding.ActivityAuthenticationBinding

class AuthenticationActivity : AppCompatActivity() {

    lateinit var binding: ActivityAuthenticationBinding
    lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        callbackManager = CallbackManager.Factory.create()

        if (null == savedInstanceState) {
            initFragment(LoginFragment.newInstance())
        }
    }

    private fun initFragment(loginFragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.grp_content_frame, loginFragment)
        transaction.commit()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}
