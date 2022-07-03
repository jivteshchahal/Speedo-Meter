package com.chahal.speedometer.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData

class NetworkCheck(val context: Context): LiveData<Boolean>() {
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var connectivityManager: ConnectivityManager

    override fun onActive() {
        super.onActive()
        checkInternet()

        connectivityManager.registerDefaultNetworkCallback(
            networkCallback)
    }

    override fun onInactive() {
        super.onInactive()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (exception: Exception) {
            Log.e("Network Callback", exception.toString())
        }

    }
    private fun checkInternet(){
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()

         networkCallback = object : ConnectivityManager.NetworkCallback() {
            // network is available for use

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                postValue(true)
            }

            // Network capabilities have changed for the network
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            }

            // lost network connection
            override fun onLost(network: Network) {
                super.onLost(network)
                Toast.makeText(context.applicationContext, "Internet Lost", Toast.LENGTH_SHORT)
                    .show()
                postValue(false)
            }
        }
        val connectivityManager =
            context.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }
}