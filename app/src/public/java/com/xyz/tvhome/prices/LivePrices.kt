package com.xyz.tvhome.prices

import android.content.Context

/**
 * An empty LivePrices: it fetches nothing, so every price card shows "–" and there is no source line.
 *
 * To show real prices, replace this class with the example below (uncomment it and delete the empty class), point
 * PRICES_URL at your own source, adapt [read] to its format, and build as usual. The example:
 * - fetches once an hour while Home is shown, and after a failure again in 5, 15 and 30 minutes, then hourly;
 * - keeps each price with the time it was read, so a source that breaks shows as stale on the page instead of an old
 *   price passing for today's; a price out of range (PriceBoard checks it) is not taken, the last good one stays;
 * - waits while the TV's clock is unset (after a power cut it starts in 2020), and fetches as soon as the network comes
 *   back (backOnline), as the TV waking from standby is often a few seconds ahead of its Wi-Fi;
 * - logs failures under the tag "TvHome".
 */
class LivePrices(context: Context, private val changed: () -> Unit) : Prices {

    override val source: String? = null

    override fun start() {}

    override fun stop() {}

    override fun board() = PriceBoard()
}

/*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.xyz.tvhome.network.HttpClient
import org.json.JSONObject
import kotlin.concurrent.thread

class LivePrices(context: Context, private val changed: () -> Unit) : Prices {

    private val prefs = context.getSharedPreferences("prices", Context.MODE_PRIVATE)
    private val handler = Handler(Looper.getMainLooper())
    private val tick = Runnable { fetch() }
    private var fetching = false
    private var shown = false

    // The line at the foot of every page: name where the prices come from.
    override val source: String? = "Nguồn tham khảo: …"

    override fun start() {
        handler.removeCallbacks(tick)
        handler.postDelayed(tick, untilDue())
        shown = true
    }

    override fun stop() {
        handler.removeCallbacks(tick)
        shown = false
    }

    // The network came back while Home is shown: fetch now if the last fetch failed or one is due.
    override fun backOnline() {
        if (shown && (prefs.getInt("failures", 0) > 0 || untilDue() == 0L)) {
            handler.removeCallbacks(tick)
            fetch()
        }
    }

    // Ms until the next fetch is due: an hour after a full read, sooner after failures (see delay).
    private fun untilDue(): Long {
        val since = System.currentTimeMillis() - prefs.getLong("checked", 0)
        val wait = delay(prefs.getInt("failures", 0))
        return if (since >= wait || since < 0) 0 else wait - since
    }

    override fun board() = PriceBoard(
        sjc = gold("sjc"), ring = gold("ring"),
        e10 = fuel("e10"), e5 = fuel("e5"), diesel = fuel("do"),
    )

    // Kept in đồng per lượng, with the time each was read (0: never).
    private fun gold(key: String): GoldPrice? {
        val t = prefs.getLong("${key}_time", 0)
        if (t == 0L) return null
        return GoldPrice(prefs.getLong("${key}_buy", 0), prefs.getLong("${key}_sell", 0), t)
    }

    // Kept in đồng per litre.
    private fun fuel(key: String): FuelPrice? {
        val t = prefs.getLong("${key}_time", 0)
        if (t == 0L) return null
        return FuelPrice(prefs.getInt(key, 0), t)
    }

    private fun fetch() {
        if (fetching) return
        if (System.currentTimeMillis() < CLOCK_SET) {
            // The clock is not set yet: HTTPS would fail and the times would be wrong. HomeActivity calls start()
            // again when the clock is set.
            Log.w(LOG, "Prices: the clock is not set yet")
            return
        }
        fetching = true
        thread(name = "prices") {
            val now = System.currentTimeMillis()
            val e = prefs.edit()
            val read = try {
                read(HttpClient.get(PRICES_URL), now, e)
            } catch (x: Exception) {
                // Keep the last prices; their times show how old they are.
                Log.w(LOG, "Prices: $x")
                0
            }
            val failures = if (read == ALL) 0 else prefs.getInt("failures", 0) + 1
            if (failures > 0) Log.w(LOG, "Prices: read $read of $ALL, failure $failures in a row")
            e.putLong("checked", now).putInt("failures", failures)
            e.apply()
            handler.post {
                fetching = false
                changed()
                handler.removeCallbacks(tick)
                handler.postDelayed(tick, delay(failures))
            }
        }
    }

    // Reads the source's reply into prefs, each price with the time it was read; returns how many it took. This
    // example's source answers with JSON already in the app's units:
    //   {"gold": {"sjc": {"buy": 138700000, "sell": 141700000}, "ring": {"buy": …, "sell": …}},
    //    "fuel": {"e10": 28250, "e5": 27700, "do": 29120}}
    // Convert here when yours uses other units (a lượng is 10 chỉ) or another format.
    private fun read(body: String, now: Long, e: android.content.SharedPreferences.Editor): Int {
        val json = JSONObject(body)
        var read = 0
        val gold = json.optJSONObject("gold")
        for (key in listOf("sjc", "ring")) {
            val o = gold?.optJSONObject(key) ?: continue
            val buy = o.optLong("buy")
            val sell = o.optLong("sell")
            if (!GoldPrice(buy, sell, now).plausible) continue
            e.putLong("${key}_buy", buy).putLong("${key}_sell", sell).putLong("${key}_time", now)
            read++
        }
        val fuel = json.optJSONObject("fuel")
        for (key in listOf("e10", "e5", "do")) {
            val price = fuel?.optInt(key) ?: continue
            if (!FuelPrice(price, now).plausible) continue
            e.putInt(key, price).putLong("${key}_time", now)
            read++
        }
        return read
    }

    companion object {
        // Your source. HTTPS only (the app allows no plain HTTP).
        private const val PRICES_URL = "https://example.com/prices.json"
        private const val LOG = "TvHome"
        // 1 January 2026: a clock before it is unset.
        private const val CLOCK_SET = 1_767_225_600_000L
        private const val MINUTE = 60_000L
        private const val HOUR = 60 * MINUTE
        // Two gold products and three fuels.
        private const val ALL = 5

        // After a full read, the next in an hour; after failures, 5, 15 and 30 minutes, then hourly.
        private fun delay(failures: Int) = when (failures) {
            0 -> HOUR
            1 -> 5 * MINUTE
            2 -> 15 * MINUTE
            3 -> 30 * MINUTE
            else -> HOUR
        }
    }
}
*/
