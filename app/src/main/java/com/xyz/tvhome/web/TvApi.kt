package com.xyz.tvhome.web

/**
 * Everything a theme page can call in the app, as window.TV (themes/core.js calls them by these names). [Bridge] is the
 * implementation; each of its methods must also carry @JavascriptInterface, as the WebView reads that from the class
 * it is given, not from this interface. The other way, what the app calls in the page, is [PageApi].
 *
 * The calls come on the WebView's bridge thread, not the UI thread. Strings from JS can be null.
 */
interface TvApi {

    /** Opens the app with this package, e.g. "com.google.android.youtube.tv" (a dock tile's data-pkg). */
    fun launch(pkg: String?)

    /** "wifi" opens the Wi-Fi settings; anything else, the main Settings. */
    fun openSettings(which: String?)

    /** Shows the native theme picker (the page's data-action="themes" button). */
    fun openThemes()

    /** Opens the TV's own list of apps (the "Ứng dụng" tile). */
    fun openApps()

    /** Whether the network reaches the internet (the page's Wi-Fi icon). */
    fun online(): Boolean

    /** The prices as JSON, PriceBoard.toJson(): {sjc, ring: {buy, sell, t}, e10, e5, do: {price, t}}. */
    fun prices(): String

    /** Whether the theme's music is on (the music button shows .off when not). */
    fun musicOn(): Boolean

    /** Turns the theme's music on or off, and returns the new state. */
    fun toggleMusic(): Boolean
}
