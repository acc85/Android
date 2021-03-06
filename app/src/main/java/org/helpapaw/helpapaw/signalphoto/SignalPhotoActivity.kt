package org.helpapaw.helpapaw.signalphoto

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.databinding.ActivitySignalPhotoBinding

class SignalPhotoActivity : AppCompatActivity() {

    companion object {
        const val SIGNAL_KEY = "signalKey"

    }

    internal lateinit var binding: ActivitySignalPhotoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signal_photo)

        if (null == savedInstanceState) {

            val signal: Signal? = intent?.getParcelableExtra(SIGNAL_KEY)
            if (signal != null) {
                val fragment = SignalPhotoFragment.newInstance(signal)
                initFragment(fragment)
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
            val actionBar = supportActionBar
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
}