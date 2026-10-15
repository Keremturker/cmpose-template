package com.kturker.multiplatform.core.auth

import org.koin.mp.KoinPlatform.getKoin

// The Swift app calls FirebaseApp.configure() only when GoogleService-Info.plist is in the bundle and says so
// through the bridge
internal actual fun platformFirebaseAvailability(): FirebaseAvailability {
    val configured = getKoin().get<IosSignInBridge>().firebaseConfigured
    return object : FirebaseAvailability {
        override val isConfigured = configured
    }
}
