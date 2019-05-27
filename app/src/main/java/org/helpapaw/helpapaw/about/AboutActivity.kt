package org.helpapaw.helpapaw.about

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.View

import org.helpapaw.helpapaw.BuildConfig
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.databinding.ActivityAboutBinding
import org.helpapaw.helpapaw.utils.SharingUtils

/**
 * Created by Alex on 10/29/2017.
 */

class AboutActivity : AppCompatActivity() {
    lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_about)
        setSupportActionBar(binding.toolbar)

        // Adding menu icon to Toolbar
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.setDisplayShowTitleEnabled(false)
            binding.toolbarTitle.text = getString(R.string.string_about_title)
        }

        binding.tvAboutVersion.text = BuildConfig.VERSION_NAME

        binding.btnAboutContacts.setOnClickListener { SharingUtils.contactSupport(this@AboutActivity) }
    }
}