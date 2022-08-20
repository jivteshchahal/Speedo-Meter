package com.chahal.speedometer.helper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.chahal.speedometer.R
import java.text.NumberFormat
import java.util.*


@SuppressLint("ClickableViewAccessibility", "InflateParams")
class Window(val context: Service, lifeCycleOwner: LifecycleOwner) {
    private var speed = 0.0f
    private var maxSpeed = -100.0
    private lateinit var locationManager: LocationManager
    private lateinit var listener: LocationListener
    private var tvDirection: TextView
    private var tvUnits: TextView
    private var tvSpeed: TextView
    private var btnFabClose: ImageButton
    private var multiplier = 3.6f
    private lateinit var strUnits: String
    private val mView: View
    private var mParams: WindowManager.LayoutParams? = null
    private val mWindowManager: WindowManager
    private val layoutInflater: LayoutInflater
    private var loc: MutableLiveData<Location> = MutableLiveData<Location>()

    //    private val MAX_CLICK_DURATION = 1000
    private var startClickTime: Long = 0
    fun open() {
        try {
            // check if the view is already
            // inflated or present in the window
            if (mView.windowToken == null) {
                if (mView.parent == null) {
                    mWindowManager.addView(mView, mParams)
                }
            }
        } catch (e: Exception) {
            Log.d("Error1", e.toString())
        }
    }

    fun close() {
        try {
            // remove the view from the window
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).removeView(mView)
            // invalidate the view
            mView.invalidate()
            // remove all views
            (mView.parent as ViewGroup).removeAllViews()

            // the above steps are necessary when you are adding and removing
            // the view simultaneously, it might give some exceptions
            locationManager.removeUpdates(listener)
        } catch (e: Exception) {
            Log.d("Error2", e.toString())
        }
    }

    init {
        // set the layout parameters of the window
        mParams = WindowManager.LayoutParams( // Shrink the window to wrap the content rather
            // than filling the screen
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,  // Display it on top of other application windows
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,  // Don't let it grab the input focus
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,  // Make the underlying application window visible
            // through any transparent parts
            PixelFormat.TRANSLUCENT
        )
        // getting a LayoutInflater
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // inflating the view with the custom layout we created
        mView = layoutInflater.inflate(R.layout.floating_button, null)
        // set onClickListener on the remove button, which removes
        // the view from the window
//        mView.findViewById<View>(R.id.fabSpeed).setOnClickListener { close() }
        tvSpeed = mView.findViewById(R.id.fabSpeed)
        btnFabClose = mView.findViewById(R.id.btnFabClose)
        tvUnits = mView.findViewById(R.id.fabUnits)
        tvDirection = mView.findViewById(R.id.fabDirection)
        // Define the position of the
        // window within the screen
        mParams!!.gravity = Gravity.END
        mParams!!.x = 0
        mParams!!.y = 100
        mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager.addView(mView, mParams)
        btnFabClose.setOnClickListener {
            //closes this window
            close()
            //stop service which creates this window
            context.stopForeground(true)
            context.stopSelf()
        }
        mView.findViewById<LinearLayoutCompat>(R.id.root_container)
            .setOnTouchListener(object : View.OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f

                override
                fun onTouch(v: View?, event: MotionEvent): Boolean {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = mParams!!.x
                            initialY = mParams!!.y
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            startClickTime = Calendar.getInstance().timeInMillis
                            return false
                        }
                        MotionEvent.ACTION_UP -> {
//                            val Xdiff = (event.rawX - initialTouchX).toInt()
//                            val Ydiff = (event.rawY - initialTouchY).toInt()
//                            val clickDuration: Long = Calendar.getInstance().timeInMillis - startClickTime
//                            if (clickDuration > MAX_CLICK_DURATION) {
//                            }
                            return false
                        }
                        MotionEvent.ACTION_MOVE -> {
                            mParams!!.x = initialX + (event.rawX - initialTouchX).toInt()
                            mParams!!.y = initialY + (event.rawY - initialTouchY).toInt()
                            mWindowManager.updateViewLayout(mView, mParams)
                            return false
                        }
                    }
                    return false
                }
            })
        getLocation()
        observeLocation(lifeCycleOwner)
    }

    private fun observeLocation(lifeCycleOwner: LifecycleOwner) {
        loc.observe(lifeCycleOwner) { location: Location ->
            speed = location.speed
            if (maxSpeed < speed) {
                maxSpeed = speed.toDouble()
            }
            val numberFormat = NumberFormat.getNumberInstance()
            numberFormat.maximumFractionDigits = 0
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            when (preferences.getString(context.getString(R.string.key_unit_type), "Km/h").toString()) {
                context.getString(R.string.string_kmph) -> {
                    multiplier = 3.6f
                    strUnits = context.getString(R.string.string_kmph)
                }
                context.getString(R.string.string_mph) -> {
                    multiplier = 2.25f
                    strUnits = context.getString(R.string.string_mph)
                }
                context.getString(R.string.string_mps) -> {
                    multiplier = 1.0f
                    strUnits = context.getString(R.string.string_mps)
                }
            }
            tvUnits.text = strUnits
            tvSpeed.text = numberFormat.format(speed.toDouble() * multiplier)
            numberFormat.maximumFractionDigits = 0
            if (location.hasBearing()) {
                val bearing = location.bearing.toDouble()
                var strDirection = context.getString(R.string.string_directionNil)
                if (bearing < 20.0) {
                    strDirection = context.getString(R.string.string_directionSNorth)
                } else if (bearing < 65.0) {
                    strDirection = context.getString(R.string.string_directionSNE)
                } else if (bearing < 110.0) {
                    strDirection = context.getString(R.string.string_directionSsE)
                } else if (bearing < 155.0) {
                    strDirection = context.getString(R.string.string_directionSSE)
                } else if (bearing < 200.0) {
                    strDirection = context.getString(R.string.string_directionSS)
                } else if (bearing < 250.0) {
                    strDirection = context.getString(R.string.string_directionSSW)
                } else if (bearing < 290.0) {
                    strDirection = context.getString(R.string.string_directionSsW)
                } else if (bearing < 345.0) {
                    strDirection = context.getString(R.string.string_directionSNW)
                } else if (bearing < 361.0) {
                    strDirection = context.getString(R.string.string_directionSN)
                }
                tvDirection.text = strDirection
            } else {
                tvDirection.text = context.getString(R.string.string_directionNA)
            }
            val nf = NumberFormat.getInstance()
            nf.maximumFractionDigits = 4
        }
    }

    private fun getLocation() {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                loc.value = location
            }

            override fun onProviderEnabled(provider: String) {
                tvSpeed.text = context.getString(R.string.string_providerStr)
            }

            override fun onProviderDisabled(provider: String) {
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            locationPermission()
            return
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, listener)
        }
    }
}