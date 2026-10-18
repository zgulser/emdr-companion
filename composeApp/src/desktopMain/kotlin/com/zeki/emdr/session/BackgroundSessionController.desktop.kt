package com.zeki.emdr.session

actual object BackgroundSessionController {
    actual fun start(playing: Boolean) = Unit
    actual fun stop() = Unit
}
