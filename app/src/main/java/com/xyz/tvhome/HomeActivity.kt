package com.xyz.tvhome

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.KeyEvent
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import com.xyz.tvhome.music.Music
import com.xyz.tvhome.network.NetworkWatch
import com.xyz.tvhome.prices.Prices
import com.xyz.tvhome.remote.RemoteKeys
import com.xyz.tvhome.theme.Theme
import com.xyz.tvhome.theme.ThemePanel
import com.xyz.tvhome.theme.Themes
import com.xyz.tvhome.web.Bridge
import com.xyz.tvhome.web.Page
import kotlin.math.roundToInt

/**
 * The home screen: a theme page from assets/ drawn in a WebView ([Page]), with the native [ThemePanel] over
 * it. Kotlin keeps what the page cannot do on its own: remote keys ([RemoteKeys], passed to the panel while it is
 * open, else to the page as nav(...)), the calls in [Bridge] (launching apps, the TV's app list, settings, the network
 * state), the hourly [Prices] and the theme's [Music].
 */
class HomeActivity : Activity() {

    private lateinit var page: Page
    private lateinit var panel: ThemePanel
    private lateinit var prices: Prices
    private lateinit var music: Music
    private lateinit var source: TextView

    /**
     * The network while Home is shown: the page's Wi-Fi icon is redrawn as it comes and goes (TV.online() reads it),
     * and the prices retry when it comes back.
     */
    private val network by lazy {
        NetworkWatch(this) { back ->
            page.networkChanged()
            if (back) prices.backOnline()
        }
    }

    /**
     * The clock was set or the time zone changed. After a power cut the TV comes up in 2020 until it syncs its clock,
     * so the page drew that day: redraw it all (the countdowns too), and fetch prices now if that makes them due.
     */
    private val clock = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            prices.start()
            page.refresh()
        }
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val themes = Themes(this)
        page = Page(this)
        music = Music.create(this)
        panel = ThemePanel(this, themes) {
            page.load(it.id)
            tint(it)
            music.theme(it.music)
        }
        prices = Prices.create(this) { page.pricesChanged() }
        page.expose(Bridge(this, prices, music) { panel.open() })
        setContentView(FrameLayout(this).apply {
            addView(page.view)
            source = sourceLine()
            addView(source, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
                .apply { bottomMargin = stagePx(12f) })
            addView(panel.view)
        })
        page.load(themes.current().id)
        tint(themes.current())
        music.theme(themes.current().music)
    }

    /**
     * Where the prices come from, small and fixed at the foot of every theme, outside the page. Sized like the pages
     * (px on their 1920×1080 stage), it sits in the bottom margin every theme leaves (56 px or more).
     */
    private fun sourceLine() = TextView(this).apply {
        text = prices.source
        setTextSize(TypedValue.COMPLEX_UNIT_PX, stagePx(16f).toFloat())
        includeFontPadding = false
    }

    /** Text only, so it takes the theme's ink (dark on light themes, light on dark ones), faded. Hidden for a theme
     *  without prices, and when the build's LivePrices names no source (the empty one). */
    private fun tint(theme: Theme) {
        source.visibility = if (theme.style.showSource && prices.source != null) View.VISIBLE else View.GONE
        val ink = theme.style.footInk
        source.setTextColor(Color.argb(140, Color.red(ink), Color.green(ink), Color.blue(ink)))
    }

    /** A size on the pages' 1920×1080 stage, in screen px, scaled as core.js scales the pages. */
    private fun stagePx(v: Float): Int {
        val dm = resources.displayMetrics
        return (v * minOf(dm.widthPixels / 1920f, dm.heightPixels / 1080f)).roundToInt()
    }

    override fun onResume() {
        super.onResume()
        page.resume()
        prices.start()
        music.show()
        page.refresh()
        network.start()
        registerReceiver(clock, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        })
    }

    override fun onPause() {
        prices.stop()
        network.stop()
        unregisterReceiver(clock)
        music.hide()
        panel.close(animate = false)
        page.pause()
        super.onPause()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        // Home has nowhere to go back to.
    }

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        val dir = RemoteKeys.direction(e.keyCode) ?: return super.dispatchKeyEvent(e)
        if (e.action == KeyEvent.ACTION_DOWN) {
            if (panel.isOpen) panel.onKey(dir) else page.nav(dir)
        }
        return true
    }
}
