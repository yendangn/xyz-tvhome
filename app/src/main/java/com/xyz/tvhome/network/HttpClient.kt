package com.xyz.tvhome.network

import java.net.HttpURLConnection
import java.net.URL

/** Blocking HTTP GETs with a browser User-Agent; call off the UI thread. */
object HttpClient {

    private const val CONNECT_TIMEOUT = 10_000
    private const val READ_TIMEOUT = 15_000
    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36"

    /** The body as UTF-8 text; throws on a network error, a timeout or an HTTP error status (4xx/5xx). */
    fun get(url: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = CONNECT_TIMEOUT
        c.readTimeout = READ_TIMEOUT
        c.setRequestProperty("User-Agent", USER_AGENT)
        try {
            return c.inputStream.use { it.readBytes().toString(Charsets.UTF_8) }
        } finally {
            c.disconnect()
        }
    }
}
