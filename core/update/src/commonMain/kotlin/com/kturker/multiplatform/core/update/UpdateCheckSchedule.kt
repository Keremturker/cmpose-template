package com.kturker.multiplatform.core.update

private const val ONE_DAY_MILLIS = 24L * 60 * 60 * 1000

// The first launch checks, then at most once a day. While an update is required, every launch checks: reopening
// the app does not get past the "Update required" screen.
internal fun isCheckDue(
    lastCheckEpochMillis: Long?,
    nowEpochMillis: Long,
    lastWasRequired: Boolean
): Boolean {
    if (lastWasRequired || lastCheckEpochMillis == null) return true
    val elapsed = nowEpochMillis - lastCheckEpochMillis
    // A clock set back (a last check "in the future") checks again instead of waiting
    return elapsed >= ONE_DAY_MILLIS || elapsed < 0
}
