package com.chahal.speedometer.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.chahal.speedometer.R
import com.chahal.speedometer.fragments.AboutFragment

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.aboutLayout, AboutFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}