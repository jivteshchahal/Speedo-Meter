package com.chahal.speedometer.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Typeface
import android.location.*
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.chahal.speedometer.R
import com.chahal.speedometer.service.NewForegroundService
import com.google.android.material.snackbar.Snackbar
import java.text.NumberFormat
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var speed = 0.0f
    private var maxSpeed = -100.0
    private lateinit var listener: LocationListener
    private lateinit var locationManager: LocationManager
    private lateinit var tvCurrentLocation: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvMaxSpeed: TextView
    private lateinit var tvExtras: TextView
    private lateinit var tvAltitude: TextView
    private lateinit var tvDirection: TextView
    private lateinit var tvLat: TextView
    private lateinit var tvLong: TextView
    private lateinit var tvUnits: TextView
    private lateinit var btnReset: Button
    private lateinit var btnAbout: ImageButton
    private lateinit var btnSettings: ImageButton
    private lateinit var btnAboutMe: ImageButton
    private lateinit var loc: MutableLiveData<Location>
    private var multiplier = 3.6f
    private lateinit var strUnits: String
    private lateinit var numberFormat: NumberFormat
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvCurrentLocation = findViewById(R.id.tvCurrentLocation)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        tvSpeed = findViewById(R.id.tvSpeed)
        tvMaxSpeed = findViewById(R.id.tvMaxSpeed)
        tvExtras = findViewById(R.id.tvExtras)
        tvAltitude = findViewById(R.id.tvAltitude)
        tvDirection = findViewById(R.id.tvDirection)
        tvLat = findViewById(R.id.tvLat)
        tvLong = findViewById(R.id.tvLong)
        tvUnits = findViewById(R.id.tvUnits)
        btnReset = findViewById(R.id.btnReset)
        btnAbout = findViewById(R.id.btnAbout)
        btnSettings = findViewById(R.id.btnSettings)
        btnAboutMe = findViewById(R.id.btnAboutMe)
        loc = MutableLiveData<Location>()
        getLocation()
        numberFormat = NumberFormat.getNumberInstance()
        numberFormat.maximumFractionDigits = 0
        preferences = PreferenceManager.getDefaultSharedPreferences(this)
        setUnitAndFont(preferences, getString(R.string.key_speed_font))
        setUnitAndFont(preferences, getString(R.string.key_unit_type))
        setUnitAndFont(preferences, getString(R.string.key_app_theme))
        setUnitAndFont(preferences, getString(R.string.key_speed_color))
        setUnitAndFont(preferences, getString(R.string.key_unit_type))
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        btnAboutMe.setOnClickListener {
            Toast.makeText(this,getString(R.string.string_coming_soon),Toast.LENGTH_SHORT).show()
        }
        btnReset.setOnClickListener {
            strUnits = getString(R.string.string_kmph)
            speed = 0.0f
            maxSpeed = -100.0
            findViewById<RadioButton>(R.id.btnRbKmh).isChecked = true
        }
        btnAbout.setOnClickListener {
            showDialog()
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }


    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        listener = object : LocationListener {

            override fun onLocationChanged(location: Location) {
                loc.value = location
            }

            override fun onProviderEnabled(provider: String) {
                tvAccuracy.text = getString(R.string.string_providerStr)
                Log.e("Provider", provider)
            }

            override fun onProviderDisabled(provider: String) {
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, listener)
        }
        observeLocation()
    }

    private fun observeLocation() {
        loc.observe(this) { location: Location ->
            speed = location.speed
            getCurrentAddress(location)
            if (maxSpeed < speed) {
                maxSpeed = speed.toDouble()
            }
            tvUnits.text = strUnits
            val newSpeed = numberFormat.format(speed.toDouble() * multiplier).toString()
            tvSpeed.text = newSpeed
            val extraLayout = findViewById<ConstraintLayout>(R.id.extrasLayout)
            if (location.extras != null) {
                extraLayout.visibility = View.VISIBLE
                val sat = location.extras.getInt(getString(R.string.key_extras_sat))
                    .toString() + getString(
                    R.string.string_sat
                )
                tvExtras.text = sat
            } else {
                extraLayout.visibility = View.INVISIBLE
            }
            val alt = numberFormat.format(location.altitude) + getString(
                R.string.string_accuracym
            )
            tvAltitude.text = alt
            val maxSpeed =
                getString(R.string.string_threedpoint).format(maxSpeed * multiplier) + " " + strUnits
            tvMaxSpeed.text = maxSpeed
            if (location.hasAltitude()) {
                val accuracy = numberFormat.format(location.accuracy.toDouble()) + getString(
                    R.string.string_accuracym
                )
                tvAccuracy.text = accuracy
            } else {
                tvAccuracy.text = getString(R.string.string_directionNil)
            }
            numberFormat.maximumFractionDigits = 0
            if (location.hasBearing()) {
                val bearing = location.bearing.toDouble()
                var strDirection = getString(R.string.string_directionNil)
                if (bearing < 20.0) {
                    strDirection = getString(R.string.string_directionNorth)
                } else if (bearing < 65.0) {
                    strDirection = getString(R.string.string_directionNE)
                } else if (bearing < 110.0) {
                    strDirection = getString(R.string.string_directionE)
                } else if (bearing < 155.0) {
                    strDirection = getString(R.string.string_directionSE)
                } else if (bearing < 200.0) {
                    strDirection = getString(R.string.string_directionS)
                } else if (bearing < 250.0) {
                    strDirection = getString(R.string.string_directionSW)
                } else if (bearing < 290.0) {
                    strDirection = getString(R.string.string_directionW)
                } else if (bearing < 345.0) {
                    strDirection = getString(R.string.string_directionNW)
                } else if (bearing < 361.0) {
                    strDirection = getString(R.string.string_directionN)
                }
                tvDirection.text = strDirection
            } else {
                tvDirection.text = getString(R.string.string_directionNA)
            }
            val nf = NumberFormat.getInstance()
            nf.maximumFractionDigits = 4
            val lat = nf.format(location.latitude) + getString(R.string.string_lat)
            tvLat.text = lat
            val long = nf.format(location.longitude) + getString(R.string.string_longi)
            tvLong.text = long
        }
    }

    private fun getCurrentAddress(location: Location) {
        val geocoder = Geocoder(applicationContext, Locale.getDefault())
        try {
            val addresses: List<Address> = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            val address: String =
                addresses[0].getAddressLine(0)
            tvCurrentLocation.text = address
        } catch (e: Exception) {
            tvCurrentLocation.text = getString(R.string.string_offline_gps)
        }
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
                    Toast.makeText(
                        this,
                        getString(R.string.string_toastCourseLoc),
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
                else -> {
                    // No location access granted.
                    Toast.makeText(
                        this,
                        getString(R.string.string_toastNoPermission),
                        Toast.LENGTH_LONG
                    )
                        .show()
//                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    val uri: Uri = Uri.fromParts("package", packageName, null)
//                    intent.data = uri
//                    startActivity(intent)
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

    @Throws(PackageManager.NameNotFoundException::class)
    private fun showDialog() {
        val dialog = AppCompatDialog(this)
        dialog.setContentView(R.layout.about_dialog)
        val tvAppName: TextView? = dialog.findViewById(R.id.tvAppName)
        val tvDescription: TextView? = dialog.findViewById(R.id.tvDialogDes)
        if (tvAppName != null) {
            val appName = getString(R.string.app_name) + " " + packageManager.getPackageInfo(
                packageName,
                0
            ).versionName
            tvAppName.text = appName
        }
        dialog.setTitle(
            "About Speedometer "
                    + packageManager.getPackageInfo(packageName, 0).versionName
        )
        tvDescription!!.text = getString(R.string.licence)
        dialog.setCancelable(true)
        dialog.show()
    }


    private fun startService() {
        // check if the user has already granted
        // the Draw over other apps permission
        if (Settings.canDrawOverlays(this)) {
            // start the service based on the android version
            startForegroundService(Intent(this, NewForegroundService::class.java))
        }
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            // send user to the device settings
            val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivity(myIntent)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (sharedPreferences != null) {
            setUnitAndFont(sharedPreferences, key)
        }

    }

    private fun setUnitAndFont(preferences: SharedPreferences, key: String?) {
        if (key.equals(getString(R.string.key_unit_type))) {
            when (preferences.getString(getString(R.string.key_unit_type), "Km/h").toString()) {
                getString(R.string.string_kmph) -> {
                    multiplier = 3.6f
                    strUnits = getString(R.string.string_kmph)
                }
                getString(R.string.string_mph) -> {
                    multiplier = 2.25f
                    strUnits = getString(R.string.string_mph)
                }
                getString(R.string.string_mps) -> {
                    multiplier = 1.0f
                    strUnits = getString(R.string.string_mps)
                }
            }
            tvUnits.text = strUnits
        } else if (key.equals(getString(R.string.key_speed_font))) {
            val items = resources.getStringArray(R.array.speedFontAlias)
            var typeface: Typeface? = ResourcesCompat.getFont(this, R.font.texazgxxaq)
            when (preferences.getString(
                getString(R.string.key_speed_font),
                getString(R.string.string_default1)
            ).toString()) {
                items[0] -> {
                    typeface = ResourcesCompat.getFont(this, R.font.texazgxxaq)!!
                }
                items[1] -> {
                    typeface = ResourcesCompat.getFont(this, R.font.harryp)!!
                }
                items[2] -> {
                    typeface = ResourcesCompat.getFont(this, R.font.grounsp7r)!!
                }
                items[3] -> {
                    typeface = ResourcesCompat.getFont(this, R.font.paintingwithchocolate5mo)!!
                }
                items[4] -> {
                    typeface = ResourcesCompat.getFont(this, R.font.essentialarrangement3p)!!
                }
                items[5] -> {
                    typeface = ResourcesCompat.getFont(this, R.font.essentialr)!!
                }
                items[6] -> {
                    typeface = ResourcesCompat.getFont(this, R.font.essentialrrangementy3)!!
                }
            }
            tvSpeed.typeface = typeface

        } else if (key.equals(getString(R.string.key_speed_color))) {
            val items = resources.getStringArray(R.array.speedColorAlias)
            when (preferences.getString(
                getString(R.string.key_speed_color),
                getString(R.string.string_default1)
            ).toString()) {
                items[0] -> {
                    // Get the primary text color of the theme
                    val typedValue = TypedValue()
                    theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)
                    val arr: TypedArray = obtainStyledAttributes(
                        typedValue.data, intArrayOf(
                            android.R.attr.textColorPrimary
                        )
                    )
                    val primaryColor = Integer.toHexString(arr.getColor(0, -1))
                    arr.recycle()
                    tvSpeed.setTextColor(Color.parseColor("#$primaryColor"))
                }
                items[1] -> {
                    tvSpeed.setTextColor(Color.parseColor(items[1]))
                }
                items[2] -> {
                    tvSpeed.setTextColor(Color.parseColor(items[2]))
                }
                items[3] -> {
                    tvSpeed.setTextColor(Color.parseColor(items[3]))
                }
                items[4] -> {
                    tvSpeed.setTextColor(Color.parseColor(items[4]))
                }
            }
        } else if (key.equals(getString(R.string.key_float_speed))) {
            val floatingSpeed = preferences.getBoolean(getString(R.string.key_float_speed), false)
            if (floatingSpeed) {
                checkOverlayPermission()
                startService()
            } else {
                stopService(Intent(this, NewForegroundService::class.java))
            }
        } else if (key.equals(getString(R.string.key_app_theme))) {
            val appTheme = preferences.getBoolean(getString(R.string.key_app_theme), false)
            if (appTheme) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(listener)
    }

    override fun onResume() {
        super.onResume()
        getLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(listener)
    }
}