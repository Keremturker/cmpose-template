package com.kturker.multiplatform.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.kturker.multiplatform.core.designsystem.theme.LocalColorsPalette

// A remote or local image through the app's Coil loader (core/image); a neutral fill while it loads or fails
@Composable
fun KtAsyncImage(
    image: Any,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    val fill = ColorPainter(LocalColorsPalette.current.toggleBackground)
    AsyncImage(
        model = image,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = fill,
        error = fill,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}
