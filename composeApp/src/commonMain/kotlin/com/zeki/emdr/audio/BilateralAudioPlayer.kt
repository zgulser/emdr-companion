package com.zeki.emdr.audio

import com.zeki.emdr.model.AudioSound
import com.zeki.emdr.session.TrajectoryEdge

interface BilateralAudioPlayer {
    fun setSound(sound: AudioSound)
    fun setArmed(armed: Boolean)
    fun triggerEdge(edge: TrajectoryEdge)
    fun fadeOut()
    fun release()
}

expect fun createBilateralAudioPlayer(): BilateralAudioPlayer

class NoOpBilateralAudioPlayer : BilateralAudioPlayer {
    override fun setSound(sound: AudioSound) = Unit
    override fun setArmed(armed: Boolean) = Unit
    override fun triggerEdge(edge: TrajectoryEdge) = Unit
    override fun fadeOut() = Unit
    override fun release() = Unit
}
