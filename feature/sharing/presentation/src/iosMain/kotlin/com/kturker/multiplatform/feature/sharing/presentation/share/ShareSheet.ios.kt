package com.kturker.multiplatform.feature.sharing.presentation.share

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.Foundation.NSBundle
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.popoverPresentationController

@OptIn(ExperimentalForeignApi::class)
internal actual fun presentShareSheet(text: String) {
    // keyWindow is deprecated for multi-scene apps; this app has a single scene (UIApplicationSupportsMultipleScenes)
    @Suppress("DEPRECATION")
    val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    val presenter = root.topmost()
    val sheet = UIActivityViewController(activityItems = listOf(text), applicationActivities = null)
    // On iPad the sheet is a popover and needs an anchor: the middle of the screen, without an arrow
    sheet.popoverPresentationController?.let { popover ->
        val view = presenter.view
        popover.sourceView = view
        view.bounds.useContents { popover.sourceRect = CGRectMake(size.width / 2, size.height / 2, 0.0, 0.0) }
        popover.permittedArrowDirections = 0uL
    }
    presenter.presentViewController(sheet, animated = true, completion = null)
}

// A sheet or dialog already shown would refuse a second presentation from the root
private fun UIViewController.topmost(): UIViewController =
    presentedViewController?.topmost() ?: this

internal actual fun appDisplayName(): String {
    val info = NSBundle.mainBundle.infoDictionary.orEmpty()
    return info["CFBundleDisplayName"] as? String ?: info["CFBundleName"] as? String ?: ""
}
