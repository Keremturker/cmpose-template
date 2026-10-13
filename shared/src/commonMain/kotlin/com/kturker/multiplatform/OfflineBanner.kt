package com.kturker.multiplatform

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kturker.multiplatform.core.domain.ConnectivityMonitor
import org.koin.compose.getKoin

// A thin band while the device is offline. Projects without the network module have no ConnectivityMonitor,
// so nothing is drawn there.
@Composable
internal fun OfflineBanner(text: String) {
    val monitor = getKoin().getOrNull<ConnectivityMonitor>() ?: return
    val isOnline by monitor.isOnline.collectAsState()
    AnimatedVisibility(visible = !isOnline) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer)
                .statusBarsPadding()
                .padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}
