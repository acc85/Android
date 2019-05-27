package org.helpapaw.helpapaw.repository

interface ISettingsRepository {

    fun getRadius(): Int

    fun getTimeout(): Int

    fun getLastShownLatitude(): Double

    fun setLastShownLatitude(latitude: Double)

    fun getLastShownLongitude(): Double

    fun setLastShownLongitude(longitude: Double)

    fun setLastShownZoom(zoom: Float)

    fun getLastShownZoom(): Float

    fun saveRadius(radius: Int)

    fun saveTimeout(timeout: Int)

    fun clearLocationData()
}
