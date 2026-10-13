package com.kturker.multiplatform.core.about

enum class LegalLinkKind {
    PrivacyPolicy,
    Terms
}

data class LegalLink(val kind: LegalLinkKind, val url: String)

fun legalLinks(
    privacyPolicyUrl: String,
    termsUrl: String
): List<LegalLink> = listOfNotNull(
    privacyPolicyUrl.trim().takeIf { it.isNotEmpty() }?.let { LegalLink(kind = LegalLinkKind.PrivacyPolicy, url = it) },
    termsUrl.trim().takeIf { it.isNotEmpty() }?.let { LegalLink(kind = LegalLinkKind.Terms, url = it) }
)
