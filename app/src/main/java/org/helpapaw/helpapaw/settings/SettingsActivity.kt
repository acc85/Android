package org.helpapaw.helpapaw.settings

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding
    internal var mSettingsFragment: SettingsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
    }

    override fun onStart() {
        super.onStart()
        initialize()
    }

    private fun initialize() {
        if (mSettingsFragment == null) {
            mSettingsFragment = SettingsFragment.newInstance()
            val fragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.grp_content_frame, mSettingsFragment)
            transaction.commit()
        }
    }
}
