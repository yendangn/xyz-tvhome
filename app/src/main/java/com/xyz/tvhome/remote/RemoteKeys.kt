package com.xyz.tvhome.remote

import android.view.KeyEvent

/** Remote keys as the directions core.js's nav(...) takes. */
object RemoteKeys {

    /** "left" | "right" | "up" | "down" | "ok" | "back", or null for a key Home leaves to the system. */
    fun direction(keyCode: Int): String? = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_LEFT -> "left"
        KeyEvent.KEYCODE_DPAD_RIGHT -> "right"
        KeyEvent.KEYCODE_DPAD_UP -> "up"
        KeyEvent.KEYCODE_DPAD_DOWN -> "down"
        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> "ok"
        KeyEvent.KEYCODE_BACK -> "back"
        else -> null
    }
}
