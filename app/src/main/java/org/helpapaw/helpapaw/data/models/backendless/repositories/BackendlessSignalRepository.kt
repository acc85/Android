package org.helpapaw.helpapaw.data.models.backendless.repositories

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
import java.util.*
class BackendlessSignalRepository:SignalRepository{

    private var signalsDatabase: SignalsDatabase = SignalsDatabase.getDatabase(PawApplication.getContext())

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
            categories.plus(category)
        }
        Backendless.Geo.savePoint(signal.latitude, signal.longitude, categories, meta, object : AsyncCallback<GeoPoint> {
            override fun handleResponse(geoPoint: GeoPoint) {
                val signalTitle:String = geoPoint.getMetadata(SIGNAL_TITLE) as String

                val dateSubmittedString = geoPoint.getMetadata(SIGNAL_DATE_SUBMITTED)
                val dateSubmitted = Date(dateSubmittedString as Long)
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
                    callback.onStatusFailure(PawApplication.getContext().getString(R.string.error_empty_signal_response))
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
                        val newSignalStatusString = geoPoint?.getMetadata(SIGNAL_STATUS)
                        val newSignalStatusInt = newSignalStatusString as Int

                        // Update signal in database
                        val signalsFromDB = signalsDatabase.signalDao().getSignal(signalId)
                        if (signalsFromDB.isNotEmpty()) {
                            val signal = signalsFromDB[0]
                            signal.status = newSignalStatusInt
                            signalsDatabase.signalDao().saveSignal(signal)
                        }

                        callback.onStatusUpdated(newSignalStatusInt)
                    }
                })
            }
        })
    }

    override fun markSignalsAsSeen(signals: List<Signal>) {
        val signalIds:Array<String> = arrayOf()
        for (i in signals.indices) {
            val signal = signals[i]
            signalIds[i] = signal.id
        }

        val signalsFromDB = signalsDatabase.signalDao().getSignals(signalIds)
        for (signal in signalsFromDB) {
            signal.seen = true
            signalsDatabase.signalDao().saveSignal(signal)
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
    }

    private fun getCategory(): String? {
        return if (PawApplication.TEST_VERSION) {
            "Debug"
        } else {
            // Category should only be added if it's not Default
            null
        }
    }

}