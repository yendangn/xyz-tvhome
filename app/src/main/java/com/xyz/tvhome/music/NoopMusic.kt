package com.xyz.tvhome.music

/** No music: nothing plays, and the page's music button stays off. For a build made with -Pmusic=noop. */
object NoopMusic : Music {

    override val on = false

    override fun toggle() = false

    override fun theme(music: String?) {}

    override fun show() {}

    override fun hide() {}
}
