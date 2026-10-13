package com.kturker.multiplatform.core.image

import androidx.compose.runtime.Composable
import coil3.compose.setSingletonImageLoaderFactory
import com.kturker.multiplatform.core.domain.BuildEnvironment
import org.koin.compose.koinInject

// Call once in MainScreen, before any image: every AsyncImage (and KtAsyncImage) uses this loader
@Composable
fun ImageLoaderSetup(environment: BuildEnvironment = koinInject()) {
    setSingletonImageLoaderFactory { context -> newImageLoader(context = context, isDebug = environment.isDebug) }
}
