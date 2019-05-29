package org.helpapaw.helpapaw.repository
import android.location.Location
import android.util.Log

import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.messaging.DeliveryOptions
import com.backendless.messaging.MessageStatus
import com.backendless.messaging.PublishOptions
import com.backendless.persistence.DataQueryBuilder
import com.backendless.push.DeviceRegistrationResult


import java.util.ArrayList

class BackendlessPushNotificationsRepository(
        val settingsRepository: ISettingsRepository

) : PushNotificationsRepository {

    override fun registerDeviceForToken() {
        Backendless.Messaging.registerDevice(object : AsyncCallback<DeviceRegistrationResult> {
            override fun handleResponse(response: DeviceRegistrationResult) {
                //Save device-token in preferences
                settingsRepository.saveTokenToPreferences(response.deviceToken)

            }

            override fun handleFault(fault: BackendlessFault) {
                Log.e(TAG, "Device registration fault: " + fault.message)
            }
        })
    }

    /*
     * Queries Backendless for locally saved device-token,
     * and updates 4 properties on the corresponding db-entry
     */
    override fun saveNewDeviceLocation(location: Location) {
        // Get local device-token
        val localToken = settingsRepository.getTokenFromPreferences()

        // Make sure localToken exists
        if (localToken != null) {

            // Build query
            val whereClause = "deviceToken = '$localToken'"
            val queryBuilder = DataQueryBuilder.create()
            queryBuilder.whereClause = whereClause

            Backendless.Data.of("DeviceRegistration").find(queryBuilder, object : AsyncCallback<List<MutableMap<Any?, Any?>>> {
                        override fun handleResponse(foundDevices: List<MutableMap<Any?, Any?>>) {
                            // every loaded object from the "DeviceRegistration" table
                            // is now an individual java.util.Map

                            // Extract 'Map' object from the 'List<Map>'
                            val mapFoundDevice = foundDevices[0]
                            try {
                                mapFoundDevice["signalRadius"] = settingsRepository.getRadius()
                                mapFoundDevice["lastLatitude"] = location.latitude
                                mapFoundDevice["lastLongitude"] = location.longitude
                                mapFoundDevice["signalTimeout"] = settingsRepository.getTimeout()
                            } catch (e: Error) {
                                Log.e(TAG, e.message)
                            }

                            // Save updated object
                            Backendless.Persistence.of("DeviceRegistration").save(mapFoundDevice, object : AsyncCallback<Map<*, *>> {
                                override fun handleResponse(response: Map<*, *>) {
                                    Log.d(TAG, "obj updated")
                                }

                                override fun handleFault(fault: BackendlessFault) {
                                    Log.d(TAG, fault.message)
                                }
                            })
                        }

                        override fun handleFault(fault: BackendlessFault) {
                            // an error has occurred, the error code can be retrieved with fault.getCode()
                            Log.d(TAG, fault.message)
                        }
                    })
        } else {
            Log.d(TAG, "localToken is null -or- non-existent")
        }
    }

    /*
     * Sends a notification to all devices within a certain distance
     */
    override fun pushNewSignalNotification(tickerText: String, message: String, signalId: String, latitude: Double, longitude: Double) {

        // Get local device-token, latitude & longitude (from settings)
        val localToken = settingsRepository.getTokenFromPreferences()

        // Build query
        val whereClause = "distance( " + latitude + ", " + longitude + ", " +
                "lastLatitude, lastLongitude ) < signalRadius * 1000"
        val queryBuilder = DataQueryBuilder.create()
        queryBuilder.whereClause = whereClause

        Backendless.Data.of("DeviceRegistration").find(queryBuilder,
                object : AsyncCallback<List<Map<*, *>>> {
                    override fun handleResponse(devices: List<Map<*, *>>) {

                        val notifiedDevices = ArrayList<String>()

                        // Iterates through all devices, excludes itself
                        for (device in devices) {
                            val deviceToken = device["deviceToken"]!!.toString()

                            if (deviceToken != localToken) {
                                notifiedDevices.add(device["deviceId"]!!.toString())
                            }
                        }

                        // Checks to see if there are any devices
                        if (notifiedDevices.size > 0) {

                            // Creates delivery options
                            val deliveryOptions = DeliveryOptions()
                            deliveryOptions.pushSinglecast = notifiedDevices

                            // Creates publish options
                            val publishOptions = PublishOptions()
                            publishOptions.putHeader("android-ticker-text", tickerText)
                            publishOptions.putHeader("android-content-text", message)
                            publishOptions.putHeader("ios-alert", message)
                            publishOptions.putHeader("ios-badge", "1")
                            publishOptions.putHeader("signalId", signalId)
                            publishOptions.putHeader("ios-category", "kNotificationCategoryNewSignal")


                            // Delivers notification
                            Backendless.Messaging.publish(message, publishOptions, deliveryOptions, object : AsyncCallback<MessageStatus> {
                                override fun handleResponse(response: MessageStatus) {
                                    Log.d(TAG, response.messageId)
                                }

                                override fun handleFault(fault: BackendlessFault) {
                                    Log.d(TAG, fault.message)
                                }
                            })

                        }
                    }

                    override fun handleFault(fault: BackendlessFault) {
                        Log.d(TAG, fault.message)
                    }
                })
    }

    companion object {
        val TAG = BackendlessPushNotificationsRepository::class.java.simpleName
    }
}
