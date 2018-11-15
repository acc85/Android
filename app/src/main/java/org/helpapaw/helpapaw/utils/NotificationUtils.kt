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
import org.helpapaw.helpapaw.data.models.Signal
import org.helpapaw.helpapaw.data.models.Signal.Companion.SOMEBODY_ON_THE_WAY
import org.helpapaw.helpapaw.signalsmap.SignalsMapActivity

class NotificationUtils{

    companion object {

        private const val channel_id_help_needed = "channel_id_help_needed"
        private const val channel_id_somebody_on_the_way = "channel_id_somebody_on_the_way"

        fun registerNotificationChannels(context: Context) {

            // Notification channels are supported from Android 8.0 on
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel 'Help needed'
            var name: CharSequence = context.getString(R.string.txt_help_needed)
            var description = context.getString(R.string.txt_channel_description_help_needed)
            var importance = NotificationManager.IMPORTANCE_HIGH
            var mChannel = NotificationChannel(channel_id_help_needed, name, importance)
            mChannel.description = description
            mChannel.enableLights(true)
            mChannel.enableVibration(true)
            mNotificationManager.createNotificationChannel(mChannel)

            // Channel 'Somebody on the way'
            name = context.getString(R.string.txt_somebody_on_the_way)
            description = context.getString(R.string.txt_channel_description_somebody_on_the_way)
            importance = NotificationManager.IMPORTANCE_DEFAULT
            mChannel = NotificationChannel(channel_id_somebody_on_the_way, name, importance)
            mChannel.description = description
            mChannel.enableLights(true)
            mChannel.enableVibration(false)
            mNotificationManager.createNotificationChannel(mChannel)
        }

        fun showNotificationForSignal(signal: Signal, context: Context) {

            val signalCode = signal.id.hashCode()

            var status = "Status: "
            val channel_id: String
            if (signal.status == SOMEBODY_ON_THE_WAY) {
                status += context.getString(R.string.txt_somebody_is_on_the_way)
                channel_id = channel_id_somebody_on_the_way
            } else {
                status += context.getString(R.string.txt_you_help_is_needed)
                channel_id = channel_id_help_needed
            }

            val mBuilder = NotificationCompat.Builder(context, channel_id)
            mBuilder.setSmallIcon(R.drawable.ic_paw_notif)
            mBuilder.setTicker(context.getString(R.string.txt_new_signal))
            mBuilder.setContentTitle(signal.title)
            mBuilder.setDefaults(Notification.DEFAULT_ALL)
            mBuilder.setWhen(signal.dateSubmitted.time)
            mBuilder.setAutoCancel(true)
            mBuilder.setOnlyAlertOnce(true)

            val pin = BitmapFactory.decodeResource(context.resources, StatusUtils.getPinResourceForCode(signal.status))
            val largeIcon = scaleBitmapForLargeIcon(pin, context)

            mBuilder.setContentText(status)
            mBuilder.setLargeIcon(largeIcon)

            val resultIntent = Intent(context, SignalsMapActivity::class.java)
            resultIntent.putExtra(Signal.KEY_FOCUSED_SIGNAL_ID, signal.id)

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addNextIntent(resultIntent)

            val resultPendingIntent = stackBuilder.getPendingIntent(signalCode, PendingIntent.FLAG_UPDATE_CURRENT)

            mBuilder.setContentIntent(resultPendingIntent)

            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
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


}