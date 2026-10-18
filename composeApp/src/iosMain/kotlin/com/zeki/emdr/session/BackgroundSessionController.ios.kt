package com.zeki.emdr.session

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive

@OptIn(ExperimentalForeignApi::class)
actual object BackgroundSessionController {
    actual fun start(playing: Boolean) {
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        session.setActive(true, error = null)
    }

    actual fun stop() {
        // Shared AVAudioSession stays with the player so a mid-session
        // preference change does not cut foreground ticks.
    }
}
