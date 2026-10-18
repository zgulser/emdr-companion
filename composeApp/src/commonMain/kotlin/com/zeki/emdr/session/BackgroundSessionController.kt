package com.zeki.emdr.session

expect object BackgroundSessionController {
    fun start(playing: Boolean)
    fun stop()
}
