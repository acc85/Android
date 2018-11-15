package org.helpapaw.helpapaw.signalsmap

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.firebase.jobdispatcher.*
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.R.layout.activity_base
import org.helpapaw.helpapaw.base.BaseActivity
import org.helpapaw.helpapaw.base.PawApplication.Companion.TEST_VERSION
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.utils.services.BackgroundCheckJobService

class SignalsMapActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (null == savedInstanceState) {
            if (intent.hasExtra(Signal.KEY_FOCUSED_SIGNAL_ID)) {
                initFragment(SignalsMapFragment.newInstance(intent.getStringExtra(Signal.KEY_FOCUSED_SIGNAL_ID)))
            } else {
                initFragment(SignalsMapFragment.newInstance())
            }
        }
        scheduleBackgroundChecks()
    }

    override fun getToolbarTitle(): String {
        var title = getString(R.string.app_name)

        if (TEST_VERSION) {
            title += " (TEST VERSION)"
        }

        return title
    }

    private fun initFragment(signalsMapFragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.grp_content_frame, signalsMapFragment)
        transaction.commit()
    }

    override fun getLayoutId(): Int {
        return activity_base
    }

    @SuppressLint("RestrictedApi")
    override fun onBackPressed() {
        if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawers()
        } else {

            val fragmentList = supportFragmentManager.fragments
            for (fragment in fragmentList) {
                if (fragment is SignalsMapFragment) {
                    fragment.onBackPressed()
                }
            }
        }
    }

    fun getToolbar(): Toolbar {
        return binding.toolbar
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

}