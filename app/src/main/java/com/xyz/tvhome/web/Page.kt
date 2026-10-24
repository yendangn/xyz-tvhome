package com.xyz.tvhome.web

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.ConsoleMessage
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import com.xyz.tvhome.theme.Themes

/**
 * The WebView that draws a theme page. Its console goes to logcat under "TvHome".
 *
 * The page can reach only the app's own assets: a link, a redirect or a request to anywhere else is refused, so a
 * page from the web never gets window.TV.
 *
 * While Home is hidden the system may kill the page's renderer to give its memory to the app in front. The WebView
 * is then replaced, and the page is loaded again when Home comes back.
 */
class Page(private val context: Context) : PageApi {

    private val frame = FrameLayout(context).apply { setBackgroundColor(Color.BLACK) }
    private var web = create()
    private var bridge: TvApi? = null
    private var theme: String? = null
    private var shown = false
    /** The renderer was lost while hidden; [resume] loads the page again. */
    private var stale = false

    val view: View get() = frame

    /** Gives the page [bridge] as window.TV; call before [load]. */
    fun expose(bridge: TvApi) {
        this.bridge = bridge
        web.addJavascriptInterface(bridge, BRIDGE)
    }

    /** Safe from any thread. */
    fun load(theme: String) {
        frame.post {
            this.theme = theme
            web.loadUrl(Themes.url(theme))
        }
    }

    /**
     * Stops the page's timers (the clock ticks every second) and drawing while Home is hidden, so a weak TV gives
     * that CPU to the app in front. pauseTimers() is process-wide, which is fine with one WebView.
     */
    fun pause() {
        shown = false
        web.onPause()
        web.pauseTimers()
    }

    fun resume() {
        shown = true
        if (stale) reload()
        web.resumeTimers()
        web.onResume()
    }

    // ---- PageApi: what the app calls in the page (themes/core.js); documented there. ----

    override fun nav(dir: String) = js("window.nav && nav('$dir')")

    override fun refresh() = js("window.refresh && refresh()")

    override fun pricesChanged() = js("window.onPrices && onPrices()")

    override fun networkChanged() = js("window.onNetwork && onNetwork()")

    private fun js(code: String) {
        frame.post { if (!stale) web.evaluateJavascript(code, null) }
    }

    private fun create() = WebView(context).apply {
        setBackgroundColor(Color.BLACK)
        // Keys go to HomeActivity, which passes them on as nav(...).
        isFocusable = false
        settings.javaScriptEnabled = true
        // file:///android_asset/ loads without it; off, a page can't open the TV's other files.
        settings.allowFileAccess = false
        settings.mediaPlaybackRequiresUserGesture = true
        // Important while shown; the first to go when hidden and memory is short.
        setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, true)
        webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(m: ConsoleMessage): Boolean {
                Log.d("TvHome", m.message())
                return true
            }
        }
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (local(request.url.toString())) return false
                Log.w("TvHome", "Blocked navigation to ${request.url}")
                return true
            }

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                if (local(request.url.toString())) return null
                Log.w("TvHome", "Blocked request to ${request.url}")
                return WebResourceResponse("text/plain", "utf-8", 403, "Blocked", null, null)
            }

            override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                Log.w("TvHome", "Renderer gone (crash: ${detail.didCrash()}), shown: $shown")
                replace()
                // Handled: without this the whole app is killed with the renderer.
                return true
            }
        }
        frame.addView(this, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    private fun replace() {
        frame.removeView(web)
        web.destroy()
        web = create()
        bridge?.let { web.addJavascriptInterface(it, BRIDGE) }
        stale = true
        if (shown) reload()
    }

    private fun reload() {
        stale = false
        theme?.let { web.loadUrl(Themes.url(it)) }
    }

    private companion object {
        /** The page's name for the [TvApi]: window.TV. */
        const val BRIDGE = "TV"

        const val ASSETS = "file:///android_asset/"

        /** One of the app's assets; "data:" and "blob:" URLs are made by the page itself. */
        fun local(url: String) = url.startsWith(ASSETS) || url.startsWith("data:") || url.startsWith("blob:")
    }
}
