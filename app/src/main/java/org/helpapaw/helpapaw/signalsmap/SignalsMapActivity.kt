package org.helpapaw.helpapaw.signalsmap

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.widget.Toast

import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Job
import com.firebase.jobdispatcher.RetryStrategy
import com.firebase.jobdispatcher.Trigger
import org.helpapaw.helpapaw.BuildConfig

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.BaseActivity
import org.helpapaw.helpapaw.base.PawApplication
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.user.UserManager
import org.helpapaw.helpapaw.utils.services.BackgroundCheckJobService

class SignalsMapActivity : BaseActivity() {

    private var mSignalsMapFragment: SignalsMapFragment? = null
    private var numberOfTitleClicks = 0
    private var restoringActivity = false

    override val toolbarTitle: String
        get() {
            var title = getString(R.string.app_name)

            if (PawApplication.getIsTestEnvironment()!!) {
                title += " (TEST)"
            }

            return title
        }

    override val layoutId: Int
        get() = R.layout.activity_base

    val toolbar: Toolbar
        get() = binding.toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            restoringActivity = true
        }
    }

    override fun onStart() {
        super.onStart()

        if (!restoringActivity) {
            initFragment()
        }
        scheduleBackgroundChecks()

        setupEnvironmentSwitching()
        if (userManager.isLoggedIn) {
            userManager.getHasAcceptedPrivacyPolicy(object : UserManager.GetUserPropertyCallback {
                override fun onSuccess(hasAcceptedPrivacyPolicy: Any) {
                    try {
                        val accepted = hasAcceptedPrivacyPolicy as Boolean
                        if (!accepted) {
                            logOut()
                        }
                    } catch (ignored: Exception) {
                    }

                }

                override fun onFailure(message: String?) {
                    // Do nothing
                }
            })
        }
    }

    private fun setupEnvironmentSwitching() {
        binding.toolbarTitle.setOnClickListener {
            numberOfTitleClicks++
            if (numberOfTitleClicks >= 7) {
                switchEnvironment()
                numberOfTitleClicks = 0
            }
        }
    }

    private fun initFragment() {
        if (mSignalsMapFragment == null) {
            if (intent.hasExtra(Signal.KEY_FOCUSED_SIGNAL_ID)) {
                mSignalsMapFragment = SignalsMapFragment.newInstance(intent.getStringExtra(Signal.KEY_FOCUSED_SIGNAL_ID))
            } else {
                mSignalsMapFragment = SignalsMapFragment.newInstance()
            }
            val fragmentManager = supportFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.add(R.id.grp_content_frame, mSignalsMapFragment!!)
            transaction.commit()
        }
    }

    private fun reinitFragment() {
        mSignalsMapFragment = SignalsMapFragment.newInstance()
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.grp_content_frame, mSignalsMapFragment!!)
        transaction.commit()
    }

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawers()
        } else {

            val fragmentList = supportFragmentManager.fragments
            if (fragmentList != null) {
                for (fragment in fragmentList) {
                    if (fragment is SignalsMapFragment) {
                        fragment.onBackPressed()
                    }
                }
            }
        }
    }

    private fun scheduleBackgroundChecks() {
        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))

        val backgroundCheckJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(BackgroundCheckJobService::class.java)
                // uniquely identifies the job
                .setTag("BackgroundCheckJobService")
                .setRecurring(true)
                // start between 30 and 60 minutes from now
                .setTrigger(Trigger.executionWindow(15 * 60, 30 * 60))
                // overwrite an existing job with the same tag
                .setReplaceCurrent(true)
                // retry with exponential backoff
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                // constraints that need to be satisfied for the job to run
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .build()

        dispatcher.mustSchedule(backgroundCheckJob)
    }

    private fun switchEnvironment() {
        PawApplication.setIsTestEnvironment(BuildConfig.DEBUG)
        binding.toolbarTitle.text = toolbarTitle
        reinitFragment()
        Toast.makeText(this, "Environment switched", Toast.LENGTH_LONG).show()
    }
}
