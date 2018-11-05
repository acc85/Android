package org.helpapaw.helpapaw.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.helpapaw.helpapaw.BuildConfig
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.databinding.ActivityAboutBinding

class AboutActivity: AppCompatActivity() {

    lateinit var binding: ActivityAboutBinding

    override  fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = getString(R.string.string_about_title)
        binding.tvAboutVersion.text = BuildConfig.VERSION_NAME
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }
}