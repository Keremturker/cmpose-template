package com.kturker.multiplatform.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kturker.multiplatform.core.designsystem.theme.LocalColorsPalette

/**
 * A failed load: [message] centred above a [retryLabel] button that calls [onRetry]. It takes plain
 * strings, so each screen picks its texts (the showcase: LocalStringResources.current.errorMessage).
 */
@Composable
fun ErrorContent(
    message: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KtText(
            text = message,
            textAlign = TextAlign.Center,
            color = LocalColorsPalette.current.textBlack
        )

        Button(onClick = onRetry) {
            KtText(text = retryLabel)
        }
    }
}
