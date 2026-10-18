package com.zeki.emdr.session

/**
 * Notification / lock-screen transport. The active session registers
 * handlers; Android [EmdrSessionService] invokes them.
 */
object SessionRemoteControls {
    var pause: (() -> Unit)? = null
    var resume: (() -> Unit)? = null
    var stop: (() -> Unit)? = null
}
