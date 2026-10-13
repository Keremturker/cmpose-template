package com.kturker.multiplatform.core.network.connectivity

import co.touchlab.kermit.Logger
import com.kturker.multiplatform.core.domain.ConnectivityMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

private val log = Logger.withTag("Connectivity")

internal actual fun createConnectivityMonitor(): ConnectivityMonitor = IosConnectivityMonitor()

// Lives as long as the app (a Koin single), so the path monitor is never cancelled
internal class IosConnectivityMonitor : ConnectivityMonitor {

    private val state = MutableStateFlow(true)
    override val isOnline: StateFlow<Boolean> = state.asStateFlow()

    init {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_update_handler(monitor) { path ->
            state.value = nw_path_get_status(path) == nw_path_status_satisfied
            log.d { "Online: ${state.value}" }
        }
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_start(monitor)
    }
}
