package com.zeki.emdr.haptics

/**
 * Light wall-impact only. Transit between edges stays silent.
 */
interface EdgeHaptics {
    fun lightImpact()
    fun release()
}

expect fun createEdgeHaptics(): EdgeHaptics

class NoOpEdgeHaptics : EdgeHaptics {
    override fun lightImpact() = Unit
    override fun release() = Unit
}
