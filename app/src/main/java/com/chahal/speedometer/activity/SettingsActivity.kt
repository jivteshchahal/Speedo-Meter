package com.chahal.speedometer.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.chahal.speedometer.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val mListPreference = preferenceManager.findPreference<Preference>(getString(R.string.key_unit_type)) as ListPreference?
            if (mListPreference!!.value == null) {
                // to ensure we don't get a null value
                // set first value by default
                mListPreference.setValueIndex(0)
            }
            mListPreference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    Log.e("Unit_Type", newValue.toString())
                    mListPreference.setValueIndex(mListPreference.findIndexOfValue(newValue.toString()))
                    mListPreference.value = newValue as String?
                    false
                }
            val mListPreference1 =
                preferenceManager.findPreference<Preference>(getString(R.string.key_speed_font)) as ListPreference?
            if (mListPreference1!!.value == null) {
                // to ensure we don't get a null value
                // set first value by default
                mListPreference1.setValueIndex(0)
            }
            mListPreference1.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    Log.e("Speed_Font", newValue.toString())
                    mListPreference1.setValueIndex(mListPreference1.findIndexOfValue(newValue.toString()))
                    mListPreference1.value = newValue as String?
                    false
                }
//            val mSwitchPreference2 =
//                preferenceManager.findPreference<Preference>(getString(R.string.key_float_speed)) as SwitchPreference?
//                mSwitchPreference2!!.onPreferenceChangeListener =
//                    Preference.OnPreferenceChangeListener { _, newValue ->
//                        Log.e("Unit_Type", newValue.toString())
//                        false
//                    }
        }
    }
}