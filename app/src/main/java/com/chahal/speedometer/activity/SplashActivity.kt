package com.chahal.speedometer.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.chahal.speedometer.R


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val progressBar =findViewById<ProgressBar>(R.id.progressBar)
        progressBar.progress = 0
        Handler(Looper.myLooper()!!).postDelayed({
            progressBar.progress = 50
            val mainIntent = Intent(applicationContext, MainActivity::class.java)
            progressBar.progress = 100
            startActivity(mainIntent)
            finish()
        }, 2000)
    }
}