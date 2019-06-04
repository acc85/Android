package org.helpapaw.helpapaw.signalphoto

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.databinding.ActivitySignalPhotoBinding
import org.helpapaw.helpapaw.models.Signal
import org.koin.android.ext.android.inject

/**
 * Created by milen on 05/03/18.
 * Display a signal photo on full screen
 */

class SignalPhotoActivity : AppCompatActivity() {
    internal lateinit var binding: ActivitySignalPhotoBinding
    val signalPhotoFragment: SignalPhotoFragment by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signal_photo)

        if (null == savedInstanceState) {

            if (intent != null) {
                val signal = intent.getParcelableExtra<Signal>(SIGNAL_KEY)
                val bundle = Bundle()
                bundle.putParcelable(SIGNAL_DETAILS, signal)
                signalPhotoFragment.arguments = bundle
                initFragment(signalPhotoFragment)
            }
        }
        hideSystemBar()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

    private fun initFragment(signalPhotoFragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.grp_content_frame, signalPhotoFragment)
        transaction.commit()
    }

    private fun hideSystemBar() {
        val decorView = window.decorView
        // Hide the status bar.
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        try {
            val actionBar = actionBar
            actionBar!!.hide()
        } catch (ex: Exception) {
            Log.e(javaClass.simpleName, "Could not hide action bar!")
        }

    }

    override fun onBackPressed() {
        val fragmentList = supportFragmentManager.fragments
        for (fragment in fragmentList) {
            if (fragment is SignalPhotoFragment) {
                fragment.onBackPressed()
            }
        }
    }

    companion object {

        val SIGNAL_KEY = "signalKey"
    }
}