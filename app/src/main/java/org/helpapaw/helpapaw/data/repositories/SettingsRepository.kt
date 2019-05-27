package org.helpapaw.helpapaw.data.repositories

import android.content.SharedPreferences

class SettingsRepository(val preferences: SharedPreferences) : ISettingsRepository {


    companion object {
        const val RADIUS_FIELD = "signalRadius"
        const val TIMEOUT_FIELD = "signalTimeout"
        const val LAST_SHOWN_LATITUDE_FIELD = "lastShownLatitude"
        const val LAST_SHOWN_LONGITUDE_FIELD = "lastShownLongitude"
        const val LAST_SHOWN_ZOOM_FIELD = "lastShownZoom"

    }

    override fun saveRadius(radius: Int) {
        val editor = preferences.edit()
        editor.putInt(RADIUS_FIELD, radius)
        editor.apply()
    }

    override fun saveTimeout(timeout: Int) {
        val editor = preferences.edit()
        editor.putInt(TIMEOUT_FIELD, timeout)
        editor.apply()
    }

    override fun getRadius(): Int =
            preferences.getInt(RADIUS_FIELD, 10)


    override fun getTimeout(): Int =
            preferences.getInt(TIMEOUT_FIELD, 7)

    override fun getLastShownLatitude(): Double {
        val lat = preferences.getString(LAST_SHOWN_LATITUDE_FIELD, "0")
        return java.lang.Double.valueOf(lat!!)
    }

    override fun setLastShownLatitude(latitude: Double) {
        val editor = preferences.edit()
        editor.putString(LAST_SHOWN_LATITUDE_FIELD, latitude.toString())
        editor.apply()
    }

    override fun getLastShownLongitude(): Double {
        val longi = preferences.getString(LAST_SHOWN_LONGITUDE_FIELD, "0")
        return longi.toDouble()
    }

    override fun setLastShownLongitude(longitude: Double) {
        val editor = preferences.edit()
        editor.putString(LAST_SHOWN_LONGITUDE_FIELD, longitude.toString())
        editor.apply()
    }

    override fun getLastShownZoom(): Float =
            preferences.getFloat(LAST_SHOWN_ZOOM_FIELD, 0f)

    override fun setLastShownZoom(zoom: Float) {
        val editor = preferences.edit()
        editor.putFloat(LAST_SHOWN_ZOOM_FIELD, zoom)
        editor.apply()
    }

    override fun clearLocationData() {
        val editor = preferences.edit()
        editor.remove(LAST_SHOWN_LATITUDE_FIELD)
        editor.remove(LAST_SHOWN_LONGITUDE_FIELD)
        editor.remove(LAST_SHOWN_ZOOM_FIELD)
        editor.apply()
    }

}