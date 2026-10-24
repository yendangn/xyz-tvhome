package com.xyz.tvhome.web

/**
 * Everything the app calls in a theme page: functions themes/core.js puts on window, which a theme's own script may wrap.
 * [Page] is the implementation. The other way, what a page can call in the app, is [TvApi].
 *
 * Each call is safe from any thread, and does nothing on a page without that function.
 */
interface PageApi {

    /** window.nav(dir): a remote key while the theme picker is closed, "left", "right", "up", "down", "ok" or "back". */
    fun nav(dir: String)

    /**
     * window.refresh(): Home is back, or the clock was set. Redraws the date and time, prices, Wi-Fi and the music
     * button, and moves the focus to YouTube.
     */
    fun refresh()

    /** window.onPrices(): new prices are in; the page reads TV.prices() again. */
    fun pricesChanged()

    /** window.onNetwork(): the network came or went while Home is shown; the page reads TV.online() again. */
    fun networkChanged()
}
