package org.helpapaw.helpapaw.data.models.backendless.repositories

import org.helpapaw.helpapaw.data.models.Signal

interface SignalRepository {

    fun getAllSignals(latitude: Double, longitude: Double, radius: Double, callback: LoadSignalsCallback)

    fun saveSignal(signal: Signal, callback: SaveSignalCallback)

    fun updateSignalStatus(signalId: String, status: Int, callback: UpdateStatusCallback)

    fun markSignalsAsSeen(signals: List<Signal>)


    interface LoadSignalsCallback {

        fun onSignalsLoaded(signals: MutableList<Signal>)

        fun onSignalsFailure(message: String)
    }

    interface SaveSignalCallback {

        fun onSignalSaved(signal: Signal)

        fun onSignalFailure(message: String)
    }

    interface UpdateStatusCallback {

        fun onStatusUpdated(status: Int)

        fun onStatusFailure(message: String)
    }
}