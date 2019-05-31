package org.helpapaw.helpapaw.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import org.helpapaw.helpapaw.R
import org.koin.android.ext.android.inject

class SettingsActivity : AppCompatActivity() {

    private val mSettingsFragment: SettingsFragment by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onStart() {
        super.onStart()
        initialize()
    }

    private fun initialize() {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.grp_content_frame, mSettingsFragment)
        transaction.commit()
    }
}
