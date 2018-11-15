package org.helpapaw.helpapaw.utils

import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import org.helpapaw.helpapaw.base.PawApplication
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class Utils {

    companion object {
        private var instance: Utils? = null

        @Synchronized
        fun getInstance(): Utils {
            if (instance == null) {
                instance = Utils()
            }
            return instance!!
        }

        @Throws(IOException::class)
        fun getHtml(url: String): String {
            // Build and set timeout values for the request.
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()

            // Read and store the result line by line then return the entire string.
            val inputStream = connection.getInputStream()
            val html = StringBuilder()
            inputStream.bufferedReader().use { html.append(it.readLines()) }
            inputStream.close()

            return html.toString()
        }
    }

    //Validation
    fun isEmailValid(email: String): Boolean {
        val EMAIL_ADDRESS = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+"
        )

        return EMAIL_ADDRESS.matcher(email).matches()
    }

    //Network
    fun hasNetworkConnection(): Boolean {
        val connectivity = PawApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivity.allNetworkInfo
        if (info != null)
            for (i in info.indices)
                if (info[i].state == NetworkInfo.State.CONNECTED) {
                    return true
                }
        return false
    }

    //Location
    fun getDistanceBetween(latitudePointOne: Double, longitudePointOne: Double,
                           latitudePointTwo: Double, longitudePointTwo: Double): Float {
        val pointOne = Location("")
        pointOne.latitude = latitudePointOne
        pointOne.longitude = longitudePointOne

        val pointTwo = Location("")
        pointTwo.latitude = latitudePointTwo
        pointTwo.longitude = longitudePointTwo

        return pointOne.distanceTo(pointTwo)
    }

    //Dates
    fun getFormattedDate(date: Date): String {
        var formattedDate = ""

        try {
            val DETAILS_DATE_FORMAT = "dd.MM.yyyy, hh:mm a"
            val targetFormat = SimpleDateFormat(DETAILS_DATE_FORMAT, Locale.getDefault())
            formattedDate = targetFormat.format(date)
        } catch (ex: Exception) {
            Log.d(Utils::class.java.name, "Failed to parse date.")
        }

        return formattedDate
    }
}