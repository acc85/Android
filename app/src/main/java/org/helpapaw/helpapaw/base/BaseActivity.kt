package org.helpapaw.helpapaw.base

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.about.AboutActivity
import org.helpapaw.helpapaw.authentication.AuthenticationActivity
import org.helpapaw.helpapaw.data.user.UserManager
import org.helpapaw.helpapaw.databinding.ActivityBaseBinding
import org.helpapaw.helpapaw.faq.FAQsView
import org.helpapaw.helpapaw.reusable.AlertDialogFragment
import org.helpapaw.helpapaw.utils.SharingUtils
import org.helpapaw.helpapaw.utils.Utils
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var binding: ActivityBaseBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    @Inject
    lateinit var userManager: UserManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        setSupportActionBar(binding.toolbar)

        // Adding menu icon to Toolbar
        if (supportActionBar != null) {
            val indicator: VectorDrawableCompat? = VectorDrawableCompat.create(resources, R.drawable.ic_menu, theme)
            indicator?.setTint(ResourcesCompat.getColor(resources, android.R.color.white, theme))
            supportActionBar?.setHomeAsUpIndicator(indicator)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
            binding.toolbarTitle.text = getToolbarTitle()
        }

        drawerToggle = setupDrawerToggle()
        binding.drawer.addDrawerListener(drawerToggle)

        binding.navView.setNavigationItemSelectedListener(getNavigationItemSelectedListener())
    }


    fun getNavigationItemSelectedListener(): NavigationView.OnNavigationItemSelectedListener {
        return NavigationView.OnNavigationItemSelectedListener { it ->
            when (it.itemId) {
                R.id.nav_item_faqs -> {
                    it.isChecked = false
                    navigateFAQsSection()
                }
                R.id.nav_item_feedback -> {
                    SharingUtils.contactSupport(this)
                    it.isChecked = false
                }
                R.id.nav_item_about -> {
                    it.isChecked = false
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_item_sign_in_out -> {
                    if (userManager.isLoggedIn()) {
                        logOut()
                    } else {
                        logIn()
                    }
                }
                else -> {
                }
            }
            binding.drawer.closeDrawers()
            true
        }
    }

    fun logIn() {
        val intent = Intent(PawApplication.getContext(), AuthenticationActivity::class.java)
        startActivity(intent)
    }

    fun logOut() {
        if (Utils.getInstance().hasNetworkConnection()) {
            userManager.logout((object : UserManager.LogoutCallback {
                override fun onLogoutSuccess() {
                    Snackbar.make(binding.root.findViewById(R.id.fab_add_signal), R.string.txt_logout_succeeded, Snackbar.LENGTH_LONG).show()
                    binding.navView.menu.findItem(R.id.nav_item_sign_in_out).setTitle(R.string.txt_log_in)
                }

                override fun onLogoutFailure(message: String) {
                    AlertDialogFragment.showAlert(getString(R.string.txt_logout_failed), message, true, supportFragmentManager)
                }
            }))
        } else {
            AlertDialogFragment.showAlert(getString(R.string.txt_logout_failed), resources.getString(R.string.txt_no_internet), false, supportFragmentManager)
        }
    }

    fun navigateFAQsSection() {
        val intent = Intent(this, FAQsView::class.java)
        startActivity(intent)
    }

    fun setupDrawerToggle(): ActionBarDrawerToggle {
        return ActionBarDrawerToggle(this,
                binding.drawer,
                binding.toolbar,
                R.string.drawer_open,
                R.string.drawer_close)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            binding.drawer.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onResume() {
        super.onResume()
        binding.navView.menu.findItem(R.id.nav_item_sign_in_out).isChecked = false
        if (userManager.isLoggedIn()) {
            binding.navView.menu.findItem(R.id.nav_item_sign_in_out).setTitle(R.string.txt_log_out)
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

    protected abstract fun getToolbarTitle():String

    protected abstract fun getLayoutId():Int
}