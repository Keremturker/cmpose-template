import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        // Once per process; ContentView (and its view controller) is created per window.
        MainViewControllerKt.startKoinAtLaunch()
        // Google Mobile Ads lives in Swift (AdsBridge.swift); Kotlin reaches it through this bridge.
        AdsBridgeRegistrationKt.registerAdsBridge(bridge: AdsBridge())
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                // Deep links (multiplatform://…, Universal Links once set up): Kotlin opens the screen
                .onOpenURL { url in _ = DeepLinksKt.handleDeepLink(uri: url.absoluteString) }
        }
    }
}
