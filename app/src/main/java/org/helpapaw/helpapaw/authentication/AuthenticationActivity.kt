package org.helpapaw.helpapaw.authentication

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.facebook.CallbackManager
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.authentication.login.LoginFragment
import org.helpapaw.helpapaw.databinding.ActivityAuthenticationBinding

class AuthenticationActivity: AppCompatActivity(){

    internal lateinit var binding: ActivityAuthenticationBinding
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
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.grp_content_frame, loginFragment)
        transaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}