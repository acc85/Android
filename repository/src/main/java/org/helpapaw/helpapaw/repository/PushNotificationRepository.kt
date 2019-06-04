package org.helpapaw.helpapaw.repository

import android.location.Location

interface PushNotificationsRepository {
    fun registerDeviceForToken()
    fun saveNewDeviceLocation(location: Location)
    fun pushNewSignalNotification(tickerText: String, message: String?, signalId: String, latitude: Double, longitude: Double)
}
