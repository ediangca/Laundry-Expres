package com.kodego.diangca.ebrahim.laundryexpres.classes

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here
        if (remoteMessage.data.isNotEmpty()) {
            // Handle data messages if you have them
            Log.d("FCM", "Data message received: ${remoteMessage.data}")
        }

        if (remoteMessage.notification != null) {
            // Handle notification messages (display notification)
            val title = remoteMessage.notification?.title
            val message = remoteMessage.notification?.body
            Log.d("FCM", "Notification received: $title - $message")
            // Show the notification to the user
            showNotification(title, message)
        }
    }

    private fun showNotification(title: String?, message: String?) {
        // Implement logic to show push notification to user
        // For example, you can use NotificationManager to display it
    }

    override fun onNewToken(token: String) {
        // Send the token to your server to handle notifications for that device
        Log.d("FCM", "New token: $token")
        // Call your server to register the token for the user
    }
}
