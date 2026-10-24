package com.xyz.tvhome.theme

import android.graphics.Color
import org.json.JSONObject

/** A hard drop shadow, as CSS box-shadow: dx dy 0 color. */
data class Shadow(val dx: Float, val dy: Float, val color: Int)

/**
 * How the theme panel looks inside one theme, from the "panel" object of themes/themes/<id>/theme.json. Sizes are px on
 * the 1920×1080 stage the theme pages are laid out on; [ThemePanel] scales them to the screen. Fonts are paths in
 * the assets (fonts/panel/…ttf), static copies of the pages' fonts, as Android cannot read woff2.
 */
data class ThemeStyle(
    val scrim: Int,
    val panel: Int,
    val ink: Int,
    /** The source line at the foot of the screen (HomeActivity), when the page's bottom edge needs other than [ink]. */
    val footInk: Int = ink,
    /** False for a theme that shows no prices: the source line would name sources for nothing on screen. */
    val showSource: Boolean = true,
    val border: Float = 0f,
    val borderColor: Int = Color.TRANSPARENT,
    val radius: Float = 12f,
    val shadow: Shadow? = null,
    /** A soft shadow, as Android elevation. */
    val elevation: Float = 0f,
    val titleFont: String,
    val titleSize: Float,
    val titleColor: Int = ink,
    val titleRule: Float = 0f,
    val titleRuleColor: Int = ink,
    val titlePad: Float = 0f,
    val cardRadius: Float = 10f,
    val imgRadius: Float = 6f,
    val imgBorder: Float = 0f,
    val imgBorderColor: Int = Color.TRANSPARENT,
    val nameFont: String,
    val nameSize: Float,
    val tagFont: String,
    val tag: Int,
    val tagBg: Int = Color.TRANSPARENT,
    val tagBorder: Float = 0f,
    val tagRadius: Float = 0f,
    val tagPadV: Float = 0f,
    val tagPadH: Float = 0f,
    val tagTilt: Float = 0f,
    val sel: Int,
    val selWidth: Float,
    val selBg: Int,
    val selLiftX: Float = 0f,
    val selLiftY: Float = -8f,
    val selShadow: Shadow? = null,
) {
    companion object {
        /** Missing keys take the defaults above; a missing colour or font that has no default throws. */
        fun from(j: JSONObject): ThemeStyle {
            fun f(k: String, d: Float) = j.optDouble(k, d.toDouble()).toFloat()
            fun c(k: String, d: Int) = j.optString(k).takeIf { it.isNotEmpty() }?.let(::color) ?: d
            fun c(k: String) = color(j.getString(k))
            fun shadow(k: String) = j.optJSONObject(k)?.let {
                Shadow(it.getDouble("dx").toFloat(), it.getDouble("dy").toFloat(), color(it.getString("color")))
            }
            val ink = c("ink")
            return ThemeStyle(
                scrim = c("scrim"), panel = c("panel"), ink = ink, footInk = c("footInk", ink),
                showSource = j.optBoolean("showSource", true),
                border = f("border", 0f), borderColor = c("borderColor", Color.TRANSPARENT), radius = f("radius", 12f),
                shadow = shadow("shadow"), elevation = f("elevation", 0f),
                titleFont = j.getString("titleFont"), titleSize = f("titleSize", 46f), titleColor = c("titleColor", ink),
                titleRule = f("titleRule", 0f), titleRuleColor = c("titleRuleColor", ink), titlePad = f("titlePad", 0f),
                cardRadius = f("cardRadius", 10f), imgRadius = f("imgRadius", 6f),
                imgBorder = f("imgBorder", 0f), imgBorderColor = c("imgBorderColor", Color.TRANSPARENT),
                nameFont = j.getString("nameFont"), nameSize = f("nameSize", 26f),
                tagFont = j.getString("tagFont"), tag = c("tag"), tagBg = c("tagBg", Color.TRANSPARENT),
                tagBorder = f("tagBorder", 0f), tagRadius = f("tagRadius", 0f),
                tagPadV = f("tagPadV", 0f), tagPadH = f("tagPadH", 0f), tagTilt = f("tagTilt", 0f),
                sel = c("sel"), selWidth = f("selWidth", 5f), selBg = c("selBg"),
                selLiftX = f("selLiftX", 0f), selLiftY = f("selLiftY", -8f), selShadow = shadow("selShadow"),
            )
        }

        private val RGBA = Regex("""rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)\s*(?:,\s*([\d.]+)\s*)?\)""")

        /** A CSS colour as theme.json writes them: #RRGGBB, #AARRGGBB or rgb(a)(r, g, b[, a]). */
        fun color(s: String): Int {
            val m = RGBA.matchEntire(s.trim()) ?: return Color.parseColor(s.trim())
            val (r, g, b) = m.destructured
            val a = m.groupValues[4].takeIf { it.isNotEmpty() }?.toFloat() ?: 1f
            return Color.argb((a * 255).toInt(), r.toInt(), g.toInt(), b.toInt())
        }
    }
}
