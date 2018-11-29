package org.helpapaw.helpapaw.faq

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.databinding.ActivityFaqsViewBinding

class FAQsView : AppCompatActivity() {
    lateinit var binding: ActivityFaqsViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_faqs_view)
        setSupportActionBar(binding.toolbar)

        // Adding menu icon to Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarTitle.text = getString(R.string.txt_faqs_view_title)
        binding.tvFaqsText.text = HtmlCompat.fromHtml(getString(R.string.string_faq), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    //    "Q1" = "How does this app work?";
//    "A1" = "If you see a stray animal that needs help but for some reason cannot provide the help yourself you can submit a signal that marks the place and describes the situation. Other people that are nearby will receive a notification about it. Hopefully someone will react to the signal and help the animal.";
//    "Q2" = "Who are the people that will help those animals?";
//    "A2" = "Help A Paw connects a network of volunteers that care about animals - just like you!";
//    "Q3" = "How does the status of the signal change?";
//    "A3" = "When a signal is submitted it starts with status 'Help needed'. When somebody decides to answer the signal he/she changes the status to 'Somebody on the way' so that other people know. If for example the person arrives at the place but needs some assistance the status can be changed back to 'Help needed'. When the animal finally receives the needed help the signal is marked as 'Solved'. Signals are color-coded in red, orange and green according to their status.";
//    "Q4" = "Does this app track my location?";
//    "A4" = "No. Your location is obtained and used only locally on your device. It will not be recorded on a server or used with any other purpose beside notifying you of animals in need in your area.";


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return false
    }
}