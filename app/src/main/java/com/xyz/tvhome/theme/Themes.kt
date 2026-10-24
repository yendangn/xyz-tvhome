package com.xyz.tvhome.theme

import android.content.Context
import org.json.JSONArray

/**
 * A theme page in the assets (themes/<id>/index.html, which loads core.js): its name, the panel's look in it, and its
 * music (an asset path such as themes/noel/jingle-bells.m4a), if it has any.
 */
data class Theme(val id: String, val name: String, val style: ThemeStyle, val music: String? = null)

/**
 * The themes this build carries, read from themes.json (written by scripts/build-web.mjs, in panel order), and which
 * one the user picked.
 */
class Themes(context: Context) {

    private val prefs = context.getSharedPreferences("ui", Context.MODE_PRIVATE)

    val all: List<Theme> = context.assets.open("themes.json").use { input ->
        val a = JSONArray(input.readBytes().toString(Charsets.UTF_8))
        (0 until a.length()).map { i ->
            val t = a.getJSONObject(i)
            Theme(
                t.getString("id"), t.getString("name"), ThemeStyle.from(t.getJSONObject("panel")),
                t.optString("music").ifEmpty { null },
            )
        }
    }

    /** The picked theme, or the first one when it is not in this build. */
    fun current(): Theme = all.firstOrNull { it.id == prefs.getString("theme", null) } ?: all[0]

    fun select(theme: Theme) {
        prefs.edit().putString("theme", theme.id).apply()
    }

    companion object {
        fun url(id: String) = "file:///android_asset/themes/$id/index.html"
    }
}
