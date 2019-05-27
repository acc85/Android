package org.helpapaw.helpapaw.data.repositories

import android.util.Log

import com.backendless.Backendless
import com.backendless.BackendlessUser
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.geo.BackendlessGeoQuery
import com.backendless.geo.GeoPoint
import com.backendless.geo.Units

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.base.PawApplication
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.db.SignalsDatabase

import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import java.util.HashMap


/**
 * Created by iliyan on 7/28/16
 */
class BackendlessSignalRepository( private val signalsDatabase: SignalsDatabase) : SignalRepository {



    private// Category should only be added if it's not Default
    val category: String?
        get() = if (PawApplication.getIsTestEnvironment()!!) {
            "Debug"
        } else {
            null
        }

    @Throws(Throwable::class)
    protected fun finalize() {
        SignalsDatabase.destroyInstance()
    }

    override fun getAllSignals(latitude: Double, longitude: Double, radius: Double, timeout: Int, callback: SignalRepository.LoadSignalsCallback) {
        val query = BackendlessGeoQuery(latitude, longitude, radius * 1000, Units.METERS)
        query.isIncludeMeta = true

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -timeout)
        val dateSubmitted = calendar.time
        query.whereClause = String.format("dateSubmitted > %d", dateSubmitted.time)

        val category = category
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

                    val signalTitle = getToStringOrNull(geoPoint.getMetadata(SIGNAL_TITLE))
                    val dateSubmittedString = getToStringOrNull(geoPoint.getMetadata(SIGNAL_DATE_SUBMITTED))
                    val signalStatus = getToStringOrNull(geoPoint.getMetadata(SIGNAL_STATUS))

                    var dateSubmitted: Date? = null
                    try {
                        dateSubmitted = Date(java.lang.Long.valueOf(dateSubmittedString!!))
                    } catch (ex: Exception) {
                        Log.d(BackendlessSignalRepository::class.java.name, "Failed to parse signal date.")
                    }

                    var signalAuthorName: String? = null
                    var signalAuthorPhone: String? = null

                    if (geoPoint.getMetadata(SIGNAL_AUTHOR) != null) {
                        signalAuthorName = getToStringOrNull((geoPoint.getMetadata(SIGNAL_AUTHOR) as BackendlessUser).getProperty(NAME_FIELD))
                    }

                    if (geoPoint.getMetadata(SIGNAL_AUTHOR) != null) {
                        signalAuthorPhone = getToStringOrNull((geoPoint.getMetadata(SIGNAL_AUTHOR) as BackendlessUser).getProperty(PHONE_FIELD))
                    }

                    val newSignal = Signal(geoPoint.objectId, signalTitle, dateSubmitted, Integer.parseInt(signalStatus!!),
                            signalAuthorName, signalAuthorPhone, geoPoint.latitude!!, geoPoint.longitude!!, false)

                    // If signal is already in DB - keep seen status
                    val signalsFromDB = signalsDatabase.signalDao().getSignal(geoPoint.objectId)
                    if (signalsFromDB.size > 0) {
                        val signalFromDb = signalsFromDB[0]
                        newSignal.seen = signalFromDb.seen
                    }
                    signalsDatabase.signalDao().saveSignal(newSignal)

                    signals.add(newSignal)
                }
                callback.onSignalsLoaded(signals)
            }

            override fun handleFault(fault: BackendlessFault) {
                callback.onSignalsFailure(fault.message)
            }
        })
    }

    override fun saveSignal(signal: Signal, callback: SignalRepository.SaveSignalCallback) {

        val meta = HashMap<String, Any>()
        meta[SIGNAL_TITLE] = signal.title
        meta[SIGNAL_DATE_SUBMITTED] = signal.dateSubmitted.time
        meta[SIGNAL_STATUS] = signal.status
        meta[SIGNAL_AUTHOR] = Backendless.UserService.CurrentUser()

        val categories = ArrayList<String>()
        val category = category
        if (category != null) {
            categories.add(category)
        }

        Backendless.Geo.savePoint(signal.latitude, signal.longitude, categories, meta, object : AsyncCallback<GeoPoint> {
            override fun handleResponse(geoPoint: GeoPoint) {
                val signalTitle = getToStringOrNull(geoPoint.getMetadata(SIGNAL_TITLE))

                val dateSubmittedString = getToStringOrNull(geoPoint.getMetadata(SIGNAL_DATE_SUBMITTED))
                val dateSubmitted = Date(java.lang.Long.valueOf(dateSubmittedString!!))
                val signalStatus = getToStringOrNull(geoPoint.getMetadata(SIGNAL_STATUS))

                var signalAuthorName: String? = null
                var signalAuthorPhone: String? = null

                if (geoPoint.getMetadata(SIGNAL_AUTHOR) != null) {
                    signalAuthorName = getToStringOrNull((geoPoint.getMetadata(SIGNAL_AUTHOR) as BackendlessUser).getProperty(NAME_FIELD))
                }

                if (geoPoint.getMetadata(SIGNAL_AUTHOR) != null) {
                    signalAuthorPhone = getToStringOrNull((geoPoint.getMetadata(SIGNAL_AUTHOR) as BackendlessUser).getProperty(PHONE_FIELD))
                }

                val savedSignal = Signal(geoPoint.objectId, signalTitle, dateSubmitted, Integer.parseInt(signalStatus!!),
                        signalAuthorName, signalAuthorPhone, geoPoint.latitude!!, geoPoint.longitude!!, true)
                signalsDatabase.signalDao().saveSignal(savedSignal)
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

        val category = category
        if (category != null) {
            geoQuery.addCategory(category)
        }

        Backendless.Geo.getPoints(geoQuery, object : AsyncCallback<List<GeoPoint>> {
            override fun handleResponse(response: List<GeoPoint>) {
                if (response.size < 1) {
                    callback.onStatusFailure(PawApplication.getContext().getString(R.string.error_empty_signal_response))
                    return
                }

                val signalPoint = response[0]
                if (signalPoint != null) {
                    val meta = signalPoint.metadata
                    meta[SIGNAL_STATUS] = status

                    signalPoint.metadata = meta
                    Backendless.Geo.savePoint(signalPoint, object : AsyncCallback<GeoPoint> {
                        override fun handleResponse(geoPoint: GeoPoint) {
                            val newSignalStatusString = getToStringOrNull(geoPoint.getMetadata(SIGNAL_STATUS))
                            val newSignalStatusInt = Integer.parseInt(newSignalStatusString!!)

                            // Update signal in database
                            val signalsFromDB = signalsDatabase.signalDao().getSignal(signalId)
                            if (signalsFromDB.size > 0) {
                                val signal = signalsFromDB[0]
                                signal.status = newSignalStatusInt
                                signalsDatabase.signalDao().saveSignal(signal)
                            }

                            callback.onStatusUpdated(newSignalStatusInt)
                        }

                        override fun handleFault(fault: BackendlessFault) {
                            callback.onStatusFailure(fault.message)
                        }
                    })
                }
            }

            override fun handleFault(fault: BackendlessFault) {
                callback.onStatusFailure(fault.message)
            }
        })
    }

    override fun markSignalsAsSeen(signals: List<Signal>?) {
        signals?.let{s->
            val signalIds = arrayOfNulls<String>(s.size)
            for (i in s.indices) {
                val signal = s[i]
                signalIds[i] = signal.id
            }
            val signalsFromDB = signalsDatabase.signalDao().getSignals(signalIds)
            for (signal in signalsFromDB) {
                signal.seen = true
                signalsDatabase.signalDao().saveSignal(signal)
            }
        }

    }

    private fun getToStringOrNull(`object`: Any?): String? {
        return `object`?.toString()
    }

    companion object {

        private val SIGNAL_TITLE = "title"
        private val SIGNAL_DATE_SUBMITTED = "dateSubmitted"
        private val SIGNAL_STATUS = "status"
        private val SIGNAL_AUTHOR = "author"
        private val NAME_FIELD = "name"
        private val PHONE_FIELD = "phoneNumber"
    }
}