package org.helpapaw.helpapaw.privacypolicy

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.webkit.WebChromeClient
import android.webkit.WebView

import org.helpapaw.helpapaw.R

class PrivacyPolicyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
        }

        val webView = findViewById<WebView>(R.id.pp_web_view)
        webView.webChromeClient = object : WebChromeClient() {
            private var mProgress: ProgressDialog? = null

            override fun onProgressChanged(view: WebView, progress: Int) {
                if (mProgress == null) {
                    mProgress = ProgressDialog(this@PrivacyPolicyActivity)
                    mProgress!!.show()
                }
                mProgress!!.setMessage("Loading $progress%")
                if (progress == 100) {
                    mProgress!!.dismiss()
                    mProgress = null
                }
            }
        }
        webView.loadUrl(getString(R.string.url_privacy_policy))
    }

}
