package com.kturker.multiplatform.core.about

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegalLinksTest {

    // The template ships both URLs empty: no row until you fill them in (README "Before you ship")
    @Test
    fun emptyUrlsHaveNoRow() {
        assertTrue(legalLinks(privacyPolicyUrl = "", termsUrl = " ").isEmpty())
    }

    @Test
    fun onlyTheFilledUrlHasARow() {
        assertEquals(
            listOf(LegalLink(kind = LegalLinkKind.PrivacyPolicy, url = "https://example.com/privacy")),
            legalLinks(privacyPolicyUrl = "https://example.com/privacy", termsUrl = "")
        )
    }

    @Test
    fun thePrivacyPolicyComesBeforeTheTerms() {
        val kinds = legalLinks(privacyPolicyUrl = "https://a.example/p", termsUrl = "https://a.example/t")
            .map { it.kind }

        assertEquals(listOf(LegalLinkKind.PrivacyPolicy, LegalLinkKind.Terms), kinds)
    }
}
