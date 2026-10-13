package com.kturker.multiplatform.core.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow

/**
 * Whether the device has a working connection. core/network provides it (projects with the network
 * module); a screen that showed DataError.Network can load again on [reconnections].
 */
interface ConnectivityMonitor {
    val isOnline: StateFlow<Boolean>

    fun reconnections(): Flow<Unit> = isOnline.reconnections()
}

// One Unit each time the connection comes back (offline -> online); never for the first value
fun Flow<Boolean>.reconnections(): Flow<Unit> = flow {
    var wasOffline = false
    collect { online ->
        if (online && wasOffline) emit(Unit)
        wasOffline = !online
    }
}
