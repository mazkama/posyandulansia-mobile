package com.saputra.wahyuaditya.posyandulansia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    private val RC_INTENT = 100
    private val CHANNEL_ID = "PosyanduLansia"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // Jika pesan berisi data
        remoteMessage.data.isNotEmpty().let {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            // Menangani data yang diterima
        }

        // Jika pesan berisi notifikasi
        remoteMessage.notification?.let {notification ->
            val body = notification.body ?: ""
            val title = notification.title ?: ""

            Log.d("FCM", "Message Notification Body: ${notification.body}")
            // Menangani notifikasi yang diterima
            sendNotification(title, body, intent)
        }
    }

    // Function to send a notification
    private fun sendNotification(title: String, body: String, intent: Intent) {
        // Pending intent that will be triggered when notification is clicked
        val pendingIntent = PendingIntent.getActivity(
            this, RC_INTENT, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set notification sound
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        // Create a notification manager
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For Android 8.0 and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, "PosyanduLansia", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi PosyanduLansia"
                setSound(ringtoneUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Build and display the notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
//            .setLargeIcon(BitmapFactory.decodeResource(resources,android.R.drawable.ic_dialog_info))
            .setContentTitle(title)
            .setContentText(body)
//            .setSound(ringtoneUri)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(RC_INTENT, notificationBuilder.build())
    }


}