package com.chahal.speedometer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.*


class SettingsFragment2 : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
//        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mListPreference =
            preferenceManager.findPreference<Preference>("Unit_Type") as ListPreference?
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
                activity?.supportFragmentManager?.beginTransaction()!!.remove(this).commit()
                false
            }
        val mListPreference1 =
            preferenceManager.findPreference<Preference>("Speed_Font") as ListPreference?
        if (mListPreference1!!.value == null) {
            // to ensure we don't get a null value
            // set first value by default
            mListPreference.setValueIndex(0)

        }
        mListPreference1.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                Log.e("Speed font", newValue.toString())
                false
            }
        val mListPreference2 =
            preferenceManager.findPreference<Preference>("App_Theme") as SwitchPreferenceCompat?
        mListPreference2!!.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                Log.e("App_Theme", newValue.toString())
                false
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        if(activity?.onBackPressed().toString() == "true"){
//            fragmentManager?.popBackStack()
//        }
//        requireActivity().fragmentManager.popBackStack();
//        requireActivity().onBackPressed()
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}