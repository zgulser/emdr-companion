package com.zeki.emdr.haptics

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

actual fun createEdgeHaptics(): EdgeHaptics = IosEdgeHaptics()

private class IosEdgeHaptics : EdgeHaptics {
    private val generator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)

    init {
        generator.prepare()
    }

    override fun lightImpact() {
        generator.impactOccurred()
        generator.prepare()
    }

    override fun release() = Unit
}
