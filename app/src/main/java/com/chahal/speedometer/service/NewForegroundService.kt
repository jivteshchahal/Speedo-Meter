package com.chahal.speedometer.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Build
import android.os.IBinder
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.chahal.speedometer.R
import com.chahal.speedometer.Window
import java.text.NumberFormat
import java.util.*

class NewForegroundService : Service() {
    private lateinit var window :Window

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()
        window = Window(this)
        // create the custom or default notification
        // based on the android version
        startMyOwnForeground()
        // create an instance of Window class
        // and display the content on screen
        window.open()
    }

    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "example.permanence"
        val channelName = "Background Service"
        val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN)

        val manager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val manager != null;
        manager.createNotificationChannel(chan)

        val  notificationBuilder: NotificationCompat.Builder =  NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
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