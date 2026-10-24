package com.xyz.tvhome.apps

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.provider.Settings

/** Opening other apps from Home. */
object Apps {

    /**
     * The TV's own app lists, best first: the Google TV launcher's app drawer (it opens apps directly, but is gone while
     * that launcher is disabled), then Settings › Apps, then the phone-style app settings.
     */
    private val LISTS = listOf(
        Intent().setComponent(ComponentName("com.google.android.tvlauncher", "com.google.android.tvlauncher.appsview.AppsViewActivity")),
        Intent().setComponent(ComponentName("com.android.tv.settings", "com.android.tv.settings.device.apps.AppsActivity")),
        Intent(Settings.ACTION_APPLICATION_SETTINGS),
    )

    /** Opens [pkg], as a TV app when it is one. Safe to call for an app removed since Home was drawn. */
    fun launch(activity: Activity, pkg: String) {
        val pm = activity.packageManager
        val i = pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg) ?: return
        start(activity, i)
    }

    /** Opens the first of the TV's app lists that is there. */
    fun openList(activity: Activity) {
        for (i in LISTS) if (start(activity, i)) return
    }

    private fun start(activity: Activity, i: Intent): Boolean = try {
        activity.startActivity(i)
        true
    } catch (e: ActivityNotFoundException) {
        // Missing or disabled.
        false
    } catch (e: SecurityException) {
        // There but not exported.
        false
    }
}
