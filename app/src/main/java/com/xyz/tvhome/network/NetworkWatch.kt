package com.xyz.tvhome.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper

/**
 * Whether the TV reaches the internet, watched while Home is shown ([start] to [stop]): the system calls back when
 * the network changes, nothing runs in between. [changed] runs on the UI thread at each change, told whether the
 * network has just come back (the TV waking from standby is often a few seconds ahead of its Wi-Fi).
 */
class NetworkWatch(context: Context, private val changed: (back: Boolean) -> Unit) {

    private val connectivity = context.getSystemService(ConnectivityManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var watching = false

    /** Whether the network reached the internet when last heard of. */
    private var online = false

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(n: Network, caps: NetworkCapabilities) =
            update(caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))

        override fun onLost(n: Network) = update(false)
    }

    fun start() {
        online = connectivity.getNetworkCapabilities(connectivity.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        if (!watching) {
            connectivity.registerDefaultNetworkCallback(callback, handler)
            watching = true
        }
    }

    fun stop() {
        if (watching) {
            connectivity.unregisterNetworkCallback(callback)
            watching = false
        }
    }

    private fun update(now: Boolean) {
        if (!watching) return
        val back = now && !online
        online = now
        changed(back)
    }
}
