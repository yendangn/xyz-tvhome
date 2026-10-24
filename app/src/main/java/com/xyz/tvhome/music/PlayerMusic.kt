package com.xyz.tvhome.music

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.IOException

/**
 * A theme's background music ("music" in its theme.json), played with MediaPlayer: the [Music] a build uses unless it
 * is made with -Pmusic=noop. Off until the user turns it on with the page's music button (through the Bridge), and
 * the choice is kept. It loops only while Home is in front, and gives way to other sound
 * through audio focus: it pauses when another app takes the focus, and the system lowers it for a short sound.
 * Everything but [on] and [toggle] runs on the UI thread.
 */
class PlayerMusic(private val context: Context) : Music {

    private val prefs = context.getSharedPreferences("ui", Context.MODE_PRIVATE)
    private val audio = context.getSystemService(AudioManager::class.java)
    private val main = Handler(Looper.getMainLooper())
    private val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(ATTRS)
        .setOnAudioFocusChangeListener(::onFocus, main)
        .build()

    @Volatile
    override var on = prefs.getBoolean("music", false)
        private set

    private var track: String? = null
    private var shown = false
    private var held = false
    private var player: MediaPlayer? = null

    override fun toggle(): Boolean {
        val now = !on
        on = now
        prefs.edit().putBoolean("music", now).apply()
        main.post(::update)
        return now
    }

    override fun theme(music: String?) {
        if (music != track) {
            close()
            track = music
        }
        update()
    }

    override fun show() {
        shown = true
        update()
    }

    override fun hide() {
        shown = false
        update()
    }

    private fun update() {
        val path = track
        if (!on || path == null) {
            close()
            letGo()
            return
        }
        if (!shown) {
            player?.takeIf { it.isPlaying }?.pause()
            letGo()
            return
        }
        if (!held) held = audio.requestAudioFocus(focus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (!held) return
        val p = player ?: open(path) ?: return
        if (!p.isPlaying) p.start()
    }

    private fun onFocus(change: Int) {
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                held = true
                update()
            }
            // Another app plays (LOSS), or speaks for a while (LOSS_TRANSIENT): wait. Ducking is the system's.
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                player?.takeIf { it.isPlaying }?.pause()
                if (change == AudioManager.AUDIOFOCUS_LOSS) letGo() else held = false
            }
        }
    }

    private fun letGo() {
        if (held) audio.abandonAudioFocusRequest(focus)
        held = false
    }

    private fun open(path: String): MediaPlayer? {
        val p = MediaPlayer()
        return try {
            context.assets.openFd(path).use { p.setDataSource(it.fileDescriptor, it.startOffset, it.length) }
            p.setAudioAttributes(ATTRS)
            p.isLooping = true
            p.setVolume(VOLUME, VOLUME)
            p.prepare()
            player = p
            p
        } catch (e: IOException) {
            Log.w("TvHome", "Music $path: ${e.message}")
            p.release()
            null
        }
    }

    private fun close() {
        player?.release()
        player = null
    }

    private companion object {
        val ATTRS: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        /** Background music: under the TV's own volume, so it never sounds like a programme starting. */
        const val VOLUME = 0.6f
    }
}
