package com.kturker.multiplatform.core.about.licenses

// The licence list (AboutLibraries JSON). shared generates it on every build and provides it, because
// only shared sees every module's dependencies.
fun interface LicensesSource {
    suspend fun read(): String
}
