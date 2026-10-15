package com.kturker.multiplatform.core.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.compose.setSingletonImageLoaderFactory
import com.kturker.multiplatform.core.domain.BuildEnvironment
import org.koin.compose.getKoin
import org.koin.compose.koinInject

// Call once in MainScreen, before any image: every AsyncImage (and KtAsyncImage) uses this loader.
// Modules that need their own Coil components bind an ImageLoaderCustomizer in Koin.
@Composable
fun ImageLoaderSetup(environment: BuildEnvironment = koinInject()) {
    val koin = getKoin()
    val customizers = remember(koin) { koin.getAll<ImageLoaderCustomizer>() }
    setSingletonImageLoaderFactory { context ->
        newImageLoader(context = context, isDebug = environment.isDebug, customizers = customizers)
    }
}
