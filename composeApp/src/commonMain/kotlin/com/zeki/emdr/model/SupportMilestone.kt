package com.zeki.emdr.model

enum class SupportMilestone(val launchCount: Int) {
    Five(5),
    Twenty(20),
    Fifty(50),
    ;

    companion object {
        fun pending(
            appLaunchCount: Int,
            shownMilestone5: Boolean,
            shownMilestone20: Boolean,
            shownMilestone50: Boolean,
        ): SupportMilestone? = when (appLaunchCount) {
            Five.launchCount -> Five.takeUnless { shownMilestone5 }
            Twenty.launchCount -> Twenty.takeUnless { shownMilestone20 }
            Fifty.launchCount -> Fifty.takeUnless { shownMilestone50 }
            else -> null
        }
    }
}

fun SessionSettings.pendingSupportMilestone(): SupportMilestone? =
    SupportMilestone.pending(
        appLaunchCount = appLaunchCount,
        shownMilestone5 = shownMilestone5,
        shownMilestone20 = shownMilestone20,
        shownMilestone50 = shownMilestone50,
    )
