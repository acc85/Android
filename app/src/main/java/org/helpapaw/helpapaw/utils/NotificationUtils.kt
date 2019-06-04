package org.helpapaw.helpapaw.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder

import org.helpapaw.helpapaw.R
import org.helpapaw.helpapaw.models.KEY_FOCUSED_SIGNAL_ID
import org.helpapaw.helpapaw.models.SOMEBODY_ON_THE_WAY
import org.helpapaw.helpapaw.models.Signal
import org.helpapaw.helpapaw.signalsmap.SignalsMapActivity


/**
 * Created by milen on 04/11/17.
 * Centralized place to deal with notifications management
 */

const val CHANNEL_ID_HELP_NEEDED = "CHANNEL_ID_HELP_NEEDED"
const val CHANNEL_ID_SOMEBODY_ON_THE_WAY = "CHANNEL_ID_SOMEBODY_ON_THE_WAY"

object NotificationUtils {



    fun registerNotificationChannels(context: Context) {

        // Notification channels are supported from Android 8.0 on
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        // Channel 'Help needed'
        var name: CharSequence = context.getString(R.string.txt_help_needed)
        var description = context.getString(R.string.txt_channel_description_help_needed)
        var importance = NotificationManager.IMPORTANCE_HIGH
        var mChannel = NotificationChannel(CHANNEL_ID_HELP_NEEDED, name, importance)
        mChannel.description = description
        mChannel.enableLights(true)
        mChannel.enableVibration(true)
        mNotificationManager?.createNotificationChannel(mChannel)

        // Channel 'Somebody on the way'
        name = context.getString(R.string.txt_somebody_on_the_way)
        description = context.getString(R.string.txt_channel_description_somebody_on_the_way)
        importance = NotificationManager.IMPORTANCE_DEFAULT
        mChannel = NotificationChannel(CHANNEL_ID_SOMEBODY_ON_THE_WAY, name, importance)
        mChannel.description = description
        mChannel.enableLights(true)
        mChannel.enableVibration(false)
        mNotificationManager?.createNotificationChannel(mChannel)
    }

    fun showNotificationForSignal(signal: Signal, context: Context) {

        val signalCode = signal.id.hashCode()

        var status = "Status: "
        val channelId: String
        if (signal.status == SOMEBODY_ON_THE_WAY) {
            status += context.getString(R.string.txt_somebody_is_on_the_way)
            channelId = CHANNEL_ID_SOMEBODY_ON_THE_WAY
        } else {
            status += context.getString(R.string.txt_you_help_is_needed)
            channelId = CHANNEL_ID_HELP_NEEDED
        }

        val mBuilder = NotificationCompat.Builder(context, channelId)
        mBuilder.setSmallIcon(R.drawable.ic_paw_notif)
        mBuilder.setTicker(context.getString(R.string.txt_new_signal))
        mBuilder.setContentTitle(signal.title)
        mBuilder.setDefaults(Notification.DEFAULT_ALL)
        mBuilder.setWhen(signal.dateSubmitted?.time?:0L)
        mBuilder.setAutoCancel(true)
        mBuilder.setOnlyAlertOnce(true)

        val pin = BitmapFactory.decodeResource(context.resources, StatusUtils.getPinResourceForCode(signal.status))
        val largeIcon = scaleBitmapForLargeIcon(pin, context)

        mBuilder.setContentText(status)
        mBuilder.setLargeIcon(largeIcon)

        val resultIntent = Intent(context, SignalsMapActivity::class.java)
        resultIntent.putExtra(KEY_FOCUSED_SIGNAL_ID, signal.id)

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntent(resultIntent)

        val resultPendingIntent = stackBuilder.getPendingIntent(signalCode, PendingIntent.FLAG_UPDATE_CURRENT)

        mBuilder.setContentIntent(resultPendingIntent)

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(signalCode, mBuilder.build())
    }

    private fun scaleBitmapForLargeIcon(bmp: Bitmap, context: Context): Bitmap {
        val res = context.resources
        val ratio = bmp.height.toDouble() / bmp.width.toDouble()
        val height = res.getDimension(android.R.dimen.notification_large_icon_height).toInt()
        val width = (height / ratio).toInt()

        return addTransparentSideBorder(bmp, height - width)
    }

    //https://stackoverflow.com/a/15525394/2781218
    private fun addTransparentSideBorder(bmp: Bitmap, borderSize: Int): Bitmap {
        val bmpWithBorder = Bitmap.createBitmap(bmp.width + borderSize, bmp.height, bmp.config)
        val canvas = Canvas(bmpWithBorder)
        canvas.drawColor(Color.TRANSPARENT)
        canvas.drawBitmap(bmp, (borderSize / 2).toFloat(), 0f, null)
        return bmpWithBorder
    }
}