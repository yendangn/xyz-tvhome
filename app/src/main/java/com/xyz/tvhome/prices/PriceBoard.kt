package com.xyz.tvhome.prices

import org.json.JSONObject

/**
 * What a [Prices] gives the pages: the output every implementation keeps to, whatever its source. Each product is
 * null until it has been read, and the page then shows "–" in its card. Times are epoch ms, the moment the price was
 * read from the source (not when it was published): the page dims a price not read for 3 hours, and shows "–" for gold
 * older than a day or fuel older than 8 days.
 *
 * Only [toJson] turns it into what core.js reads, so an implementation never writes JSON, and a price out of range
 * (a misread field or a changed unit) never reaches the screen.
 */
data class PriceBoard(
    /** SJC gold bars. */
    val sjc: GoldPrice? = null,
    /** 999.9 gold rings. */
    val ring: GoldPrice? = null,
    /** Petrol E10 RON 95. */
    val e10: FuelPrice? = null,
    /** Petrol E5 RON 92. */
    val e5: FuelPrice? = null,
    /** Diesel DO 0.05S. */
    val diesel: FuelPrice? = null,
) {

    /**
     * {"sjc": {"buy", "sell", "t"}, "ring": …, "e10": {"price", "t"}, "e5": …, "do": …}, leaving out a product that
     * is null or out of range. core.js's onPrices() reads it through the Bridge; scripts/preview.mjs stubs the same.
     */
    fun toJson(): String = JSONObject().apply {
        for ((key, g) in listOf("sjc" to sjc, "ring" to ring)) {
            if (g != null && g.plausible) put(key, JSONObject().put("buy", g.buy).put("sell", g.sell).put("t", g.readAt))
        }
        for ((key, f) in listOf("e10" to e10, "e5" to e5, "do" to diesel)) {
            if (f != null && f.plausible) put(key, JSONObject().put("price", f.perLitre).put("t", f.readAt))
        }
    }.toString()
}

/** A gold product's buying and selling price in đồng per lượng (e.g. 142_000_000), read at [readAt] (epoch ms). */
data class GoldPrice(val buy: Long, val sell: Long, val readAt: Long) {

    /**
     * 10 to 2,000 million per lượng, buying at or under selling, and a spread of at most a fifth. Wide on purpose: it
     * stops a misread field or a wrong unit, which would be off by orders of magnitude, not a real market move.
     */
    val plausible: Boolean
        get() = buy in GOLD_RANGE && sell in GOLD_RANGE && buy <= sell && sell - buy <= sell / 5

    private companion object {
        val GOLD_RANGE = 10_000_000L..2_000_000_000L
    }
}

/** A fuel's price in đồng per litre (e.g. 29_050), read at [readAt] (epoch ms). */
data class FuelPrice(val perLitre: Int, val readAt: Long) {

    /** 10,000 to 80,000 đồng per litre; anything else is a misread. */
    val plausible: Boolean
        get() = perLitre in 10_000..80_000
}
