package com.chahal.speedometer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.chahal.speedometer.helper.Window

class NewForegroundService : LifecycleService() {
    private lateinit var window: Window

    override fun onCreate() {
        super.onCreate()
        window = Window(this,this)
        // create the custom or default notification
        // based on the android version
        startMyOwnForeground()
        // create an instance of Window class
        // and display the content on screen
        window.open()
    }
    private fun startMyOwnForeground() {
        val notificationChannelId = "example.permanence"
        val channelName = "Background Service"
        val channel = NotificationChannel(
            notificationChannelId,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )

        val manager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, notificationChannelId)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("Service running")
            .setContentText("Displaying over other apps")

            // this is important, otherwise the notification will show the way
            // you want i.e. it will show some default notification
//            .setSmallIcon(R.drawable.ic_launcher_foreground)

            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(2, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        window.close()
        stopForeground(true)
    }
}