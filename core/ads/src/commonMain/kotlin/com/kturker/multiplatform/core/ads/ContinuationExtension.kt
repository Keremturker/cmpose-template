package com.kturker.multiplatform.core.ads

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

// Ad SDK callbacks can fire after the caller left (or more than once): only the first one resumes.
internal fun <T> CancellableContinuation<T>.resumeIfActive(value: T) {
    if (isActive) resume(value)
}
