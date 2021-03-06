package org.helpapaw.helpapaw.utils.services

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.google.android.gms.location.LocationServices
import dagger.android.AndroidInjection
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.data.models.Signal.Companion.SOLVED
import org.helpapaw.helpapaw.data.models.backendless.repositories.BackendlessSignalRepository
import org.helpapaw.helpapaw.data.models.backendless.repositories.SignalRepository
import org.helpapaw.helpapaw.db.SignalDao
import org.helpapaw.helpapaw.db.SignalsDatabase
import org.helpapaw.helpapaw.signalsmap.SignalsMapPresenter.Companion.DEFAULT_SEARCH_RADIUS
import org.helpapaw.helpapaw.utils.NotificationUtils
import java.util.*
import javax.inject.Inject


class BackgroundCheckJobService : JobService() {

    internal var mCurrentNotificationIds = HashSet<String>()
    internal var mNotificationManager: NotificationManager? = null

    @Inject
    lateinit var signalRepository: BackendlessSignalRepository

    @Inject
    lateinit var signalDao: SignalDao

    companion object {
        val TAG = BackgroundCheckJobService::class.java.simpleName

        internal const val CURRENT_NOTIFICATION_IDS = "CurrentNotificationIds"
    }

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onStartJob(job: JobParameters): Boolean {

        Log.d(TAG, "onStartJob called")

        mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        //        mSharedPreferences = getApplicationContext().getSharedPreferences(TAG, Context.MODE_PRIVATE);

        // Do some work here
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            mFusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        //Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            getSignalsForLastKnownLocation(location, job)
                        } else {
                            Log.d(TAG, "got callback but last location is null")
                            jobFinished(job, true)
                        }
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "failed to get location")
                        jobFinished(job, true)
                    }
        } else {
            Log.d(TAG, "No location permission")
        }

        return true // Answers the question: "Is there still work going on?"
    }

    override fun onStopJob(job: JobParameters): Boolean {
        return true // Answers the question: "Should this job be retried?"
    }

    private fun getSignalsForLastKnownLocation(location: Location, job: JobParameters) {

        signalRepository.getAllSignals(location.latitude, location.longitude, DEFAULT_SEARCH_RADIUS.toDouble(), object : SignalRepository.LoadSignalsCallback {
            override fun onSignalsLoaded(signals: MutableList<Signal>) {

                Log.d(TAG, "got signals")

                if (!signals.isEmpty()) {

                    for (signal in signals) {
                        if (signal.status < SOLVED) {
                            val signalsFromDB = signalDao.getSignal(signal.id)
                            if (signalsFromDB?.size ?: 0 > 0) {
                                val signalFromDb = signalsFromDB?.get(0)
                                if (!signalFromDb?.seen!!) {
                                    NotificationUtils.showNotificationForSignal(signal, applicationContext)
                                    mCurrentNotificationIds.add(signal.id)
                                    signalFromDb.seen = true
                                    signalDao.saveSignal(signalFromDb)
                                }
                            }
                        }
                    }
                }

                // Cancel all previous notifications that are not currently present
                //                Set<String> oldNotificationIds = mSharedPreferences.getStringSet(CURRENT_NOTIFICATION_IDS, null);
                //                if (oldNotificationIds != null) {
                //                    for (String id : oldNotificationIds) {
                //                        if (!mCurrentNotificationIds.contains(id)) {
                //                            mNotificationManager.cancel(id.hashCode());
                //                        }
                //                    }
                //                }

                // Save ids of current notifications
                //                SharedPreferences.Editor editor = mSharedPreferences.edit();
                //                editor.putStringSet(CURRENT_NOTIFICATION_IDS, mCurrentNotificationIds);
                //                editor.apply();

                jobFinished(job, false)
            }

            override fun onSignalsFailure(message: String) {
                Log.d(TAG, "there was a problem obtaining signals: $message")
                jobFinished(job, true)
            }
        })

    }

}