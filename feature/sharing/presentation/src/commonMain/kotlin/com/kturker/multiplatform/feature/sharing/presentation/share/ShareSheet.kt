package com.kturker.multiplatform.feature.sharing.presentation.share

/** Hands a text to another app (messages, mail); the members screen shares invite links through it. */
fun interface TextSharer {
    fun share(text: String)
}

/**
 * The platform's share sheet: Android's ACTION_SEND chooser on the current Activity, iOS's
 * UIActivityViewController on the key window. A common class over [presentShareSheet] rather than an expect
 * class, which Kotlin still ships as Beta.
 */
class ShareSheet : TextSharer {
    override fun share(text: String) = presentShareSheet(text)
}

internal expect fun presentShareSheet(text: String)

/** The app's name as the home screen shows it: Android's application label, iOS's bundle display name. */
internal expect fun appDisplayName(): String

/** The text that goes with an invite link, in the app's language; [url] is the link. */
fun interface InviteText {
    fun of(url: String): String
}
