package com.xyz.tvhome.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import com.xyz.tvhome.theme.Shadow
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * What the native panels (theme picker, app list) share: sizes in px on the page's 1920×1080 stage, scaled to the
 * screen the way core.js scales the page; the panel fonts from the assets; and the theme's boxes.
 */
class PanelKit(private val context: Context) {

    var scale = 1f
        private set
    private val fonts = HashMap<String, Typeface>()

    /** Takes the scale from the view the panel covers (the activity's content). */
    fun measure(over: View) {
        scale = min(over.width / 1920f, over.height / 1080f)
    }

    fun px(v: Float) = (v * scale).roundToInt()

    /** A panel font from the assets (fonts/panel/…), loaded once. */
    fun font(path: String): Typeface = fonts.getOrPut(path) { Typeface.createFromAsset(context.assets, path) }

    /**
     * A rounded box with an optional border and hard shadow. [reserve] keeps room for a shadow that is not drawn,
     * so a card does not change size when it is picked.
     */
    fun box(fill: Int, radius: Float, border: Float, borderColor: Int, shadow: Shadow?, reserve: Shadow? = shadow): Drawable {
        val r = radius * scale
        val body = GradientDrawable().apply {
            setColor(fill)
            cornerRadius = r
            if (border > 0) setStroke(max(1, px(border)), borderColor)
        }
        if (reserve == null) return body
        val back = GradientDrawable().apply {
            setColor(shadow?.color ?: Color.TRANSPARENT)
            cornerRadius = r
        }
        val dx = px(reserve.dx)
        val dy = px(reserve.dy)
        return LayerDrawable(arrayOf(back, body)).apply {
            setLayerInset(0, dx, dy, 0, 0)
            setLayerInset(1, 0, 0, dx, dy)
        }
    }
}

/** Sets a background without letting its insets replace the view's padding. */
fun View.setBg(d: Drawable) {
    val l = paddingLeft
    val t = paddingTop
    val r = paddingRight
    val b = paddingBottom
    background = d
    setPadding(l, t, r, b)
}
