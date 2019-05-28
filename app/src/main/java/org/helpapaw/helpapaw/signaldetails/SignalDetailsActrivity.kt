package org.helpapaw.helpapaw.signaldetails

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.databinding.ActivitySignalDetailsBinding

class SignalDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignalDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_signal_details)
        setSupportActionBar(binding.toolbar)

        // Adding menu icon to Toolbar
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            //            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_close);
            supportActionBar.setDisplayShowTitleEnabled(false)
            binding.toolbarTitle.text = getString(R.string.txt_signal_details_title)
        }

        if (null == savedInstanceState) {

            if (intent != null) {
                val signal = intent.getParcelableExtra<Signal>(SIGNAL_KEY)
                val fragment = SignalDetailsFragment.newInstance(signal)
                initFragment(fragment)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }

    private fun initFragment(signalsDetailsFragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.grp_content_frame, signalsDetailsFragment)
        transaction.commit()
    }

    override fun onBackPressed() {
        val fragmentList = supportFragmentManager.fragments
        if (fragmentList != null) {
            for (fragment in fragmentList) {
                if (fragment is SignalDetailsFragment) {
                    fragment.onBackPressed()
                }
            }
        }
    }

    companion object {

        val SIGNAL_KEY = "signalKey"
    }

}
