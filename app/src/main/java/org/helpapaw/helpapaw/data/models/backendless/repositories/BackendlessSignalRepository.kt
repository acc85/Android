package org.helpapaw.helpapaw.data.models.backendless.repositories

import android.app.Application
import android.content.Context
import android.util.Log
import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.geo.BackendlessGeoQuery
import com.backendless.geo.GeoPoint
import com.backendless.geo.Units
import org.helpapaw.helpapaw.BuildConfig
import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.PawApplication
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.db.SignalDao
import org.helpapaw.helpapaw.db.SignalsDatabase
import java.util.*

class BackendlessSignalRepository():SignalRepository{

    lateinit var application: PawApplication
    lateinit var signalDao: SignalDao

    constructor(application:PawApplication, signalDao:SignalDao):this(){
        this.application = application
        this.signalDao = signalDao
    }


    companion object {
        private const val SIGNAL_TITLE = "title"
        private const val SIGNAL_DATE_SUBMITTED = "dateSubmitted"
        private const val SIGNAL_STATUS = "status"
        private const val SIGNAL_AUTHOR = "author"
        private const val NAME_FIELD = "name"
        private const val PHONE_FIELD = "phoneNumber"
    }


    override fun saveSignal(signal: Signal, callback: SignalRepository.SaveSignalCallback) {
        val meta = HashMap<String, Any>()
        meta[SIGNAL_TITLE] = signal.title
        meta[SIGNAL_DATE_SUBMITTED] = signal.dateSubmitted.time
        meta[SIGNAL_STATUS] = signal.status
        meta[SIGNAL_AUTHOR] = Backendless.UserService.CurrentUser()

        val categories = mutableListOf<String>()
        val category = getCategory()
        if (category != null) {
            categories.add(category)
        }
        Backendless.Geo.savePoint(signal.latitude, signal.longitude, categories, meta, object : AsyncCallback<GeoPoint> {
            override fun handleResponse(geoPoint: GeoPoint) {
                val signalTitle:String = geoPoint.getMetadata(SIGNAL_TITLE) as String

                val dateSubmittedString:String = geoPoint.getMetadata(SIGNAL_DATE_SUBMITTED) as String
                val dateSubmitted = Date(dateSubmittedString.toLong())
                val signalStatus:String = geoPoint.getMetadata(SIGNAL_STATUS) as String

                lateinit var signalAuthorName: String
                lateinit var signalAuthorPhone: String

                if (geoPoint.getMetadata(SIGNAL_AUTHOR) != null) {
                    signalAuthorName = (geoPoint.getMetadata(SIGNAL_AUTHOR) as BackendlessUser).getProperty(NAME_FIELD) as String
                }

                if (geoPoint.getMetadata(SIGNAL_AUTHOR) != null) {
                    signalAuthorPhone = (geoPoint.getMetadata(SIGNAL_AUTHOR) as BackendlessUser).getProperty(PHONE_FIELD) as String
                }

                val savedSignal = Signal(
                        geoPoint.objectId,
                        signalTitle,
                        dateSubmitted,
                        Integer.parseInt(signalStatus),
                        signalAuthorName,
                        signalAuthorPhone,
                        geoPoint.latitude,
                        geoPoint.longitude,
                        true)
                signalDao.saveSignal(savedSignal)
                callback.onSignalSaved(savedSignal)
            }

            override fun handleFault(backendlessFault: BackendlessFault) {
                callback.onSignalFailure(backendlessFault.message)
            }

        })
    }

    override fun updateSignalStatus(signalId: String, status: Int, callback: SignalRepository.UpdateStatusCallback) {
        val whereClause = "objectId = '$signalId'"
        val geoQuery = BackendlessGeoQuery()
        geoQuery.whereClause = whereClause
        geoQuery.isIncludeMeta = true

        val category = getCategory()
        if (category != null) {
            geoQuery.addCategory(category)
        }

        Backendless.Geo.getPoints(geoQuery, object : AsyncCallback<List<GeoPoint?>> {
            override fun handleFault(fault: BackendlessFault) {
                callback.onStatusFailure(fault.message)
            }

            override fun handleResponse(response: List<GeoPoint?>) {
                if (response.isEmpty()) {
                    callback.onStatusFailure(application.applicationContext.getString(R.string.error_empty_signal_response))
                    return
                }

                val signalPoint = response[0]

                if (signalPoint != null) {
                    val meta = signalPoint.metadata
                    meta[SIGNAL_STATUS] = status

                    signalPoint.metadata = meta
                }

                Backendless.Geo.savePoint(signalPoint, object : AsyncCallback<GeoPoint> {
                    override fun handleFault(fault: BackendlessFault) {
                        callback.onStatusFailure(fault.message)
                    }

                    override fun handleResponse(geoPoint: GeoPoint?) {
                        val newSignalStatusString:String = geoPoint?.getMetadata(SIGNAL_STATUS) as String
                        val newSignalStatusInt = newSignalStatusString.toInt()

                        // Update signal in database
                        val signalsFromDB = signalDao.getSignal(signalId)
                        if (signalsFromDB.isNotEmpty()) {
                            val signal = signalsFromDB[0]
                            signal.status = newSignalStatusInt
                            signalDao.saveSignal(signal)
                        }

                        callback.onStatusUpdated(newSignalStatusInt)
                    }
                })
            }
        })
    }

    override fun markSignalsAsSeen(signals: List<Signal>) {
        val signalIds:Array<String> = Array(signals.size){it->
            signals[it].id
        }
        val signalsFromDB = signalDao.getSignals(signalIds)
        for (signal in signalsFromDB) {
            signal.seen = true
            signalDao.saveSignal(signal)
        }
    }

    override fun getAllSignals(latitude: Double, longitude: Double, radius: Double, callback: SignalRepository.LoadSignalsCallback) {
        val query = BackendlessGeoQuery(latitude, longitude, radius, Units.METERS)
        query.isIncludeMeta = true

        // Only get signals that were created in the last 3 days
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -3)
        val dateSubmitted = calendar.time
        query.whereClause = String.format("dateSubmitted > %d", dateSubmitted.time)

        val category = getCategory()
        if (category != null) {
            query.addCategory(category)
        }

        Backendless.Geo.getPoints(query, object : AsyncCallback<List<GeoPoint>> {
            override fun handleResponse(response: List<GeoPoint>?) {
                if (response == null) {
                    return
                }

                val signals = ArrayList<Signal>()
                for (i in response.indices) {
                    val geoPoint = response[i]

                    val signalTitle:String = geoPoint.getMetadata(SIGNAL_TITLE) as String
                    val dateSubmittedString:String = geoPoint.getMetadata(SIGNAL_DATE_SUBMITTED) as String
                    val signalStatus:String = geoPoint.getMetadata(SIGNAL_STATUS) as String

                    lateinit var submittedDate: Date
                    try {
                        submittedDate = Date(dateSubmittedString.toLong())
                    } catch (ex: Exception) {
                        Log.d(BackendlessSignalRepository::class.java.name, "Failed to parse signal date.")
                    }

                    var signalAuthorName: String? = null
                    var signalAuthorPhone: String? = null

                    if (geoPoint.getMetadata(SIGNAL_AUTHOR) != null) {
                        signalAuthorName = (geoPoint.getMetadata(SIGNAL_AUTHOR) as BackendlessUser).getProperty(NAME_FIELD) as String
                    }

                    if (geoPoint.getMetadata(SIGNAL_AUTHOR) != null) {
                        signalAuthorPhone = (geoPoint.getMetadata(SIGNAL_AUTHOR) as BackendlessUser).getProperty(PHONE_FIELD) as String
                    }

                    val newSignal = Signal(geoPoint.objectId, signalTitle, submittedDate, Integer.parseInt(signalStatus),
                            signalAuthorName!!, signalAuthorPhone!!, geoPoint.latitude!!, geoPoint.longitude!!, false)

                    // If signal is already in DB - keep seen status
                    val signalsFromDB = signalDao.getSignal(geoPoint.objectId)
                    if (signalsFromDB.size > 0) {
                        val (_, _, _, _, _, _, _, _, _, seen) = signalsFromDB[0]
                        newSignal.seen = seen
                    }
                    signalDao.saveSignal(newSignal)

                    signals.add(newSignal)
                }
                callback.onSignalsLoaded(signals)
            }

            override fun handleFault(fault: BackendlessFault) {
                callback.onSignalsFailure(fault.message)
            }
        })
    }

    private fun getCategory(): String? {
        return if (BuildConfig.DEBUG) {
            "Debug"
        } else {
            // Category should only be added if it's not Default
            null
        }
    }

}