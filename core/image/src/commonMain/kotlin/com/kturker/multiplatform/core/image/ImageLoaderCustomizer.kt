package com.kturker.multiplatform.core.image

import coil3.ComponentRegistry

/** Adds Coil components (fetchers, decoders) to the app's ImageLoader; bound in Koin by the modules that need one. */
fun interface ImageLoaderCustomizer {
    fun ComponentRegistry.Builder.customize()
}
