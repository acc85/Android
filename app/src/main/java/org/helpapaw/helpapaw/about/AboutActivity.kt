package org.helpapaw.helpapaw.about

import androidx.databinding.DataBindingUtil
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders

import org.helpapaw.helpapaw.BuildConfig
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.databinding.ActivityAboutBinding
import org.helpapaw.helpapaw.utils.SharingUtils
import org.helpapaw.helpapaw.viewmodels.AboutViewModel

/**
 * Created by Alex on 10/29/2017.
 */

class AboutActivity : AppCompatActivity() {
    lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_about)
        binding.viewModel = ViewModelProviders.of(this).get(AboutViewModel::class.java)
        setSupportActionBar(binding.toolbar)

        // Adding menu icon to Toolbar
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.setDisplayShowTitleEnabled(false)
            binding.toolbarTitle.text = getString(R.string.string_about_title)
        }
    }
}