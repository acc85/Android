package org.helpapaw.helpapaw.base

import android.content.Intent
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.about.AboutActivity
import org.helpapaw.helpapaw.authentication.AuthenticationActivity
import org.helpapaw.helpapaw.data.user.UserManager
import org.helpapaw.helpapaw.databinding.ActivityBaseBinding
import org.helpapaw.helpapaw.faq.FAQsView
import org.helpapaw.helpapaw.privacypolicy.PrivacyPolicyActivity
import org.helpapaw.helpapaw.reusable.AlertDialogFragment
import org.helpapaw.helpapaw.settings.SettingsActivity
import org.helpapaw.helpapaw.utils.Injection
import org.helpapaw.helpapaw.utils.SharingUtils
import org.helpapaw.helpapaw.utils.Utils
import org.koin.android.ext.android.inject

/**
 * Created by iliyan on 6/22/16
 */
abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var binding: ActivityBaseBinding
    private var drawerToggle: ActionBarDrawerToggle? = null
    val userManager: UserManager by inject()

    // This method will trigger on item Click of navigation menu
    // Closing drawer on item click
    //    binding.drawer.closeDrawers();
    val navigationItemSelectedListener: NavigationView.OnNavigationItemSelectedListener
        get() = NavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                R.id.nav_item_sign_in_out -> if (userManager.isLoggedIn) {
                    logOut()
                } else {
                    logIn()
                }

                R.id.nav_item_faqs -> {
                    menuItem.isChecked = false
                    navigateFAQsSection()
                }

                R.id.nav_item_feedback -> {
                    SharingUtils.contactSupport(this@BaseActivity)
                    menuItem.isChecked = false
                }

                R.id.nav_item_about -> {
                    menuItem.isChecked = true
                    val aboutIntent = Intent(this@BaseActivity, AboutActivity::class.java)
                    startActivity(aboutIntent)
                }

                R.id.nav_item_privacy_policy -> {
                    menuItem.isChecked = true
                    val ppIntent = Intent(this@BaseActivity, PrivacyPolicyActivity::class.java)
                    startActivity(ppIntent)
                }

                R.id.nav_item_settings -> {
                    menuItem.isChecked = false
                    navigateSettingsSection()
                }
            }
            true
        }

    protected abstract val toolbarTitle: String

    protected abstract val layoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutId)
        setSupportActionBar(binding.toolbar)

        // Adding menu icon to Toolbar
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            val indicator = VectorDrawableCompat.create(resources, R.drawable.ic_menu, theme)
            indicator?.setTint(ResourcesCompat.getColor(resources, android.R.color.white, theme))
            supportActionBar.setHomeAsUpIndicator(indicator)
            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.setDisplayShowTitleEnabled(false)
            binding.toolbarTitle.text = toolbarTitle
        }

        drawerToggle = setupDrawerToggle()
        binding.drawer.addDrawerListener(drawerToggle!!)

        binding.navView.setNavigationItemSelectedListener(navigationItemSelectedListener)
    }

    private fun logIn() {
        val intent = Intent(PawApplication.getContext(), AuthenticationActivity::class.java)
        startActivity(intent)
    }

    protected fun logOut() {
        if (Utils.getInstance().hasNetworkConnection()) {
            userManager.logout(object : UserManager.LogoutCallback {
                override fun onLogoutSuccess() {
                    Snackbar.make(binding.root, R.string.txt_logout_succeeded, Snackbar.LENGTH_LONG).show()
                    binding.navView.menu.findItem(R.id.nav_item_sign_in_out).setTitle(R.string.txt_log_in)
                    val intent = Intent(applicationContext, AuthenticationActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun onLogoutFailure(message: String?) {
                    AlertDialogFragment.showAlert(getString(R.string.txt_logout_failed), message, true, this@BaseActivity.supportFragmentManager)
                }
            })
        } else {
            AlertDialogFragment.showAlert(getString(R.string.txt_logout_failed), resources.getString(R.string.txt_no_internet), false, this@BaseActivity.supportFragmentManager)
        }
    }

    private fun navigateSettingsSection() {
        val intent = Intent(this@BaseActivity, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateFAQsSection() {
        val intent = Intent(this, FAQsView::class.java)
        startActivity(intent)
    }

    private fun setupDrawerToggle(): ActionBarDrawerToggle {
        return ActionBarDrawerToggle(this,
                binding.drawer,
                binding.toolbar,
                R.string.drawer_open,
                R.string.drawer_close)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {

            binding.drawer.openDrawer(GravityCompat.START)
        }
        return drawerToggle!!.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggles
        drawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onResume() {
        super.onResume()

        binding.navView.menu.findItem(R.id.nav_item_sign_in_out).isChecked = false

        if (userManager.isLoggedIn) {
            binding.navView.menu.findItem(R.id.nav_item_sign_in_out).setTitle(R.string.txt_log_out)
            val title = binding.navView.getHeaderView(0).findViewById<TextView>(R.id.nav_title)
            if (title != null) {
                userManager.getUserName(object : UserManager.GetUserPropertyCallback {
                    override fun onSuccess(value: Any) {
                        if (value is String) {
                            title!!.setText(value.toString())
                        }
                    }

                    override fun onFailure(message: String?) {
                        Log.d(TAG, message)
                    }
                })
            }
        } else {
            binding.navView.menu.findItem(R.id.nav_item_sign_in_out).setTitle(R.string.txt_log_in)
        }
    }

    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    companion object {

        private val TAG = BaseActivity::class.java.simpleName
    }
}
