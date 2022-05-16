package com.example.speedometer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.text.NumberFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    var speed = 0.0f
    private var maxSpeed = -100.0
    private var locationManager: LocationManager? = null
    private lateinit var tvCurrentLocation: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvMaxSpeed: TextView
    private lateinit var tvDirection: TextView
    private lateinit var tvLat: TextView
    private lateinit var tvLong: TextView
    private lateinit var btnRGroup: RadioGroup
    private lateinit var btnReset: Button
    var multiplier = 3.6f
    var strUnits = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvCurrentLocation = findViewById(R.id.tvCurrentLocation)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        tvSpeed = findViewById(R.id.tvSpeed)
        tvMaxSpeed = findViewById(R.id.tvMaxSpeed)
        tvDirection = findViewById(R.id.tvDirection)
        tvLat = findViewById(R.id.tvLat)
        tvLong = findViewById(R.id.tvLong)
        btnRGroup = findViewById(R.id.btnRadioGroup)
        btnReset =findViewById(R.id.btnReset)
        locationPermission()
        strUnits =getString(R.string.kmph)
        getLocation()
        btnRGroup.setOnCheckedChangeListener { _, checkedId ->
            run {
                when (checkedId) {
                    R.id.btnRbKmh -> {
                        multiplier = 3.6f
                        strUnits = getString(R.string.kmph)
                    }
                    R.id.btnRbMh -> {
                        multiplier = 2.25f
                        strUnits = getString(R.string.mph)
                    }
                    R.id.btnRbMs -> {
                        multiplier = 1.0f
                        strUnits = getString(R.string.mps)
                    }
                }
            }
        }
        btnReset.setOnClickListener{
            strUnits =getString(R.string.kmph)
            speed = 0.0f
            maxSpeed =-100.0
            findViewById<RadioButton>(R.id.btnRbKmh).isChecked = true
            getLocation()
        }

    }

    private fun getLocation() {
        locationManager = (getSystemService(Context.LOCATION_SERVICE) as LocationManager?)!!
        val listener: LocationListener = object : LocationListener {
            var fitSpeed = 0f
            var localSpeed = 0f

            @SuppressLint("SetTextI18n")
            override fun onLocationChanged(location: Location) {
                getCurrentAddress(location)
                if (maxSpeed < speed) {
                    maxSpeed = speed.toDouble()
                }
                localSpeed = speed * multiplier
                fitSpeed = filter(fitSpeed, localSpeed)
                val numberFormat = NumberFormat.getNumberInstance()
                numberFormat.maximumFractionDigits = 0
                tvSpeed.text = numberFormat.format(speed.toDouble() * multiplier)  + strUnits
                tvMaxSpeed.text = "" + maxSpeed.toFloat() * multiplier + strUnits
                if (location.hasAltitude()) {
                    tvAccuracy.text = numberFormat.format(location.accuracy.toDouble()) + getString(
                        R.string.accuracym)
                } else {
                    tvAccuracy.text = getString(R.string.directionNil)
                }
                numberFormat.maximumFractionDigits = 0
                if (location.hasBearing()) {
                    val bearing = location.bearing.toDouble()
                    var strDirection = getString(R.string.directionNil)
                    if (bearing < 20.0) {
                        strDirection = getString(R.string.directionNorth)
                    } else if (bearing < 65.0) {
                        strDirection = getString(R.string.directionNE)
                    } else if (bearing < 110.0) {
                        strDirection = getString(R.string.directionE)
                    } else if (bearing < 155.0) {
                        strDirection = getString(R.string.directionSE)
                    } else if (bearing < 200.0) {
                        strDirection = getString(R.string.directionS)
                    } else if (bearing < 250.0) {
                        strDirection = getString(R.string.directionSW)
                    } else if (bearing < 290.0) {
                        strDirection = getString(R.string.directionW)
                    } else if (bearing < 345.0) {
                        strDirection = getString(R.string.directionNW)
                    } else if (bearing < 361.0) {
                        strDirection = getString(R.string.directionN)
                    }
                    tvDirection.text = strDirection
                } else {
                    tvDirection.text = getString(R.string.directionNA)
                }
                val nf = NumberFormat.getInstance()
                nf.maximumFractionDigits = 4
                tvLat.text = nf.format(location.latitude) + getString(R.string.lat)
                tvLong.text = nf.format(location.longitude) + getString(R.string.longi)
            }

            override fun onProviderEnabled(provider: String) {
                tvSpeed.text = getString(R.string.providerStr)
            }

            override fun onProviderDisabled(provider: String) {
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            locationPermission()
            return
        } else {
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, listener)
        }
    }

    private fun getCurrentAddress(location: Location) {
        val addresses: List<Address>
        val geocoder = Geocoder(applicationContext, Locale.getDefault())

        addresses = geocoder.getFromLocation(
            location.latitude,
            location.longitude,
            1
        )
        val address: String =
            addresses[0].getAddressLine(0)
        tvCurrentLocation.text = address
        speed = location.speed
    }

    private fun filter(prev: Float, curr: Float): Float {
        // If first time through, initialise digital filter with current values
        if (java.lang.Float.isNaN(prev)) return curr
        // If current value is invalid, return previous filtered value
        return if (java.lang.Float.isNaN(curr)) prev else (curr / 2 + prev * (1.0 - 1.0 / 2)).toFloat()
        // Calculate new filtered value
    }

    private fun locationPermission() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Precise location access granted.
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                    Toast.makeText(this,"Fine location needed for accurate data",Toast.LENGTH_LONG).show()
                }
                else -> {
                    // No location access granted.
                    Toast.makeText(this,"Location access needed for the App",Toast.LENGTH_LONG).show()
                }
            }
        }
// Before you perform the actual permission request, check whether your app
// already has the permissions, and whether your app needs to show a permission
// rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
}