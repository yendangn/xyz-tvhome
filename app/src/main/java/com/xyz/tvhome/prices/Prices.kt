package com.xyz.tvhome.prices

import android.content.Context

/**
 * Where the gold and fuel prices the pages show come from. An implementation fetches them its own way and hands them
 * over as a [PriceBoard]: fixed products, fixed units (đồng per lượng, đồng per litre) and the time each was read.
 * The board, not the implementation, becomes what the page reads, so any implementation that fills it binds to every
 * theme. The build's is [LivePrices], from app/src/public/ (empty until filled in) or app/src/local/ when there
 * (-Pprices, see app/build.gradle).
 */
interface Prices {

    /** Where the prices come from, shown at the foot of every page; null shows nothing there. */
    val source: String?

    /** Home is shown: fetch now if due, then on a schedule. */
    fun start()

    /** Home is hidden: stop fetching. */
    fun stop()

    /** The network has just come back while Home is shown (NetworkWatch): a chance to retry a failed fetch. */
    fun backOnline() {}

    /** The latest prices, a product left null until it has been read. Called from the WebView's bridge thread. */
    fun board(): PriceBoard

    companion object {
        /** The build's prices; [changed] runs on the UI thread when new ones are in. */
        fun create(context: Context, changed: () -> Unit): Prices =
            LivePrices(context, changed)
    }
}
