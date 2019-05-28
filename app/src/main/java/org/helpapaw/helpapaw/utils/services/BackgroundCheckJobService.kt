package org.helpapaw.helpapaw.utils.services

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import android.util.Log
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.google.android.gms.location.LocationServices
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.models.Signal.SOLVED
import org.helpapaw.helpapaw.repository.SettingsRepository
import org.helpapaw.helpapaw.repository.SignalRepository
import org.helpapaw.helpapaw.db.SignalsDatabase
import org.helpapaw.helpapaw.utils.NotificationUtils
import org.koin.android.ext.android.inject
import java.util.HashSet

/**
 * Created by milen on 20/08/17.
 * This class to periodically check for signals around the user and notify them if there are
 */

class BackgroundCheckJobService : JobService() {
    private var database: SignalsDatabase? = null

    internal var mCurrentNotificationIds = HashSet<String>()
    lateinit var mNotificationManager: NotificationManager
    val settingsRepository : SettingsRepository by inject()
    val signalRepositoryInstance : SignalRepository by inject()

    override fun onStartJob(job: JobParameters): Boolean {
        database = SignalsDatabase.getDatabase(this)

        Log.d(TAG, "onStartJob called")

        mNotificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

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
        database = null
        return true // Answers the question: "Should this job be retried?"
    }

    private fun getSignalsForLastKnownLocation(location: Location, job: JobParameters) {
        signalRepositoryInstance.getAllSignals(location.latitude, location.longitude, settingsRepository.getRadius().toDouble(), settingsRepository.getTimeout(), object : SignalRepository.LoadSignalsCallback {
            override fun onSignalsLoaded(signals: MutableList<Signal>?) {

                Log.d(TAG, "got signals")

                if (signals != null && !signals.isEmpty() && database != null) {

                    for (signal in signals) {
                        if (signal.status < SOLVED) {
                            val signalsFromDB = database!!.signalDao().getSignal(signal.id)
                            if (signalsFromDB.size > 0) {
                                val signalFromDb = signalsFromDB[0]
                                if (!signalFromDb.seen) {
                                    NotificationUtils.showNotificationForSignal(signal, applicationContext)
                                    mCurrentNotificationIds.add(signal.id)
                                    signalFromDb.seen = true
                                    database!!.signalDao().saveSignal(signalFromDb)
                                }
                            }
                        }
                    }
                }
                jobFinished(job, false)
            }

            override fun onSignalsFailure(message: String) {
                Log.d(TAG, "there was a problem obtaining signals: $message")
                jobFinished(job, true)
            }
        })

    }

    companion object {
        val TAG = BackgroundCheckJobService::class.java.simpleName
        internal val CURRENT_NOTIFICATION_IDS = "CurrentNotificationIds"
    }
}
