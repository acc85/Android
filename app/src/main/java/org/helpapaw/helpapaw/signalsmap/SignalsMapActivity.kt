package org.helpapaw.helpapaw.signalsmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.PermissionChecker
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.firebase.jobdispatcher.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.R.layout.activity_base
import org.helpapaw.helpapaw.base.BaseActivity
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.utils.services.BackgroundCheckJobService
import java.security.Permissions

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
//        scheduleBackgroundChecks()
    }

    override fun getToolbarTitle(): String {
        var title = getString(R.string.app_name)

        title += if (BuildConfig.DEBUG) "(TEST VERSION)" else{}

        return title
    }

    private fun initFragment(signalsMapFragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.grp_content_frame, signalsMapFragment,"SIGNAL_MAP_FRAGMENT")
        transaction.commit()
    }

    override fun getLayoutId(): Int {
        return activity_base
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        for(permission in permissions){
            if(permission == Manifest.permission.ACCESS_FINE_LOCATION){
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    val fragment: SignalsMapFragment? = supportFragmentManager.findFragmentByTag("SIGNAL_MAP_FRAGMENT") as SignalsMapFragment
                    fragment?.zoomToUserLocation()
                }
            }
        }
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
        GlobalScope.launch {
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this@SignalsMapActivity))

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

}