package com.xyz.tvhome.web

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import android.webkit.JavascriptInterface
import com.xyz.tvhome.apps.Apps
import com.xyz.tvhome.music.Music
import com.xyz.tvhome.prices.Prices

/**
 * window.TV: the [TvApi] a theme page calls, documented there. These run on the WebView's bridge thread; whatever
 * touches the UI moves to the UI thread.
 */
class Bridge(
    private val activity: Activity,
    private val prices: Prices,
    private val music: Music,
    private val openThemes: () -> Unit,
) : TvApi {

    @JavascriptInterface
    override fun launch(pkg: String?) {
        if (pkg != null) activity.runOnUiThread { Apps.launch(activity, pkg) }
    }

    @JavascriptInterface
    override fun openSettings(which: String?) {
        activity.runOnUiThread {
            try {
                activity.startActivity(Intent(if (which == "wifi") Settings.ACTION_WIFI_SETTINGS else Settings.ACTION_SETTINGS))
            } catch (ignored: ActivityNotFoundException) {
                activity.startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }
    }

    @JavascriptInterface
    override fun openThemes() {
        activity.runOnUiThread(openThemes)
    }

    @JavascriptInterface
    override fun openApps() {
        activity.runOnUiThread { Apps.openList(activity) }
    }

    @JavascriptInterface
    override fun online(): Boolean {
        val cm = activity.getSystemService(ConnectivityManager::class.java)
        val nc = cm.getNetworkCapabilities(cm.activeNetwork)
        return nc != null && nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    @JavascriptInterface
    override fun prices(): String = prices.board().toJson()

    @JavascriptInterface
    override fun musicOn(): Boolean = music.on

    @JavascriptInterface
    override fun toggleMusic(): Boolean = music.toggle()
}
