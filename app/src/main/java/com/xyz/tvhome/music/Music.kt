package com.xyz.tvhome.music

import android.content.Context
import com.xyz.tvhome.BuildConfig

/**
 * A theme's background music: the file named by "music" in its theme.json, turned on and off with the page's music
 * button (TV.musicOn(), TV.toggleMusic()). A build picks one with -Pmusic: [PlayerMusic] (the default) or [NoopMusic].
 * Everything but [on] and [toggle] is called on the UI thread.
 */
interface Music {

    /** Whether the user has the music on. Read from the bridge thread too. */
    val on: Boolean

    /** Flips [on] and keeps it; safe from any thread. Returns the new state. */
    fun toggle(): Boolean

    /** The picked theme's music (an asset path such as themes/noel/jingle-bells.m4a), or null for none. */
    fun theme(music: String?)

    /** Home is shown: play, if [on] and the theme has music. */
    fun show()

    /** Home is hidden: stop playing. */
    fun hide()

    companion object {
        fun create(context: Context): Music = if (BuildConfig.MUSIC == "noop") NoopMusic else PlayerMusic(context)
    }
}
