package com.kturker.multiplatform.core.about

import org.koin.core.annotation.Single

// What an About row shows: inject it (@Provided in a ViewModel) instead of reading the constants
interface AppInfo {
    val version: AppVersion
    val legalLinks: List<LegalLink>
}

@Single(binds = [AppInfo::class])
internal class DefaultAppInfo : AppInfo {
    override val version: AppVersion by lazy { currentAppVersion() }
    override val legalLinks: List<LegalLink> = legalLinks(privacyPolicyUrl = PRIVACY_POLICY_URL, termsUrl = TERMS_URL)
}
