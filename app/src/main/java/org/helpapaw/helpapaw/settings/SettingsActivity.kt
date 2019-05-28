package org.helpapaw.helpapaw.settings

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

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
            transaction.add(R.id.grp_content_frame, mSettingsFragment!!)
            transaction.commit()
        }
    }
}
