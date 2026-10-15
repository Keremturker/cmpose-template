package com.kturker.multiplatform.feature.sharing.presentation

import com.kturker.multiplatform.core.navigation.parseDeepLink
import com.kturker.multiplatform.feature.sharing.contract.InviteDestination
import com.kturker.multiplatform.feature.sharing.presentation.data.inviteUrl
import com.kturker.multiplatform.feature.sharing.presentation.navigation.SharingDeepLink
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SharingDeepLinkTest {

    private fun parse(
        uri: String,
        appLinkHost: String = ""
    ) = parseDeepLink(uri = uri, scheme = "myapp", appLinkHost = appLinkHost, routes = listOf(SharingDeepLink()))

    @Test
    fun anInviteLinkOpensTheInviteScreen() {
        assertEquals(InviteDestination("abc"), parse("myapp://invite/abc"))
    }

    // The same route through an https App/Universal Link once APP_LINK_HOST is set; the token is its one segment
    @Test
    fun anInviteLinkCarriesItsToken() {
        assertEquals(InviteDestination("Ab_9-x"), parse("myapp://invite/Ab_9-x"))
        assertEquals(
            InviteDestination("Ab_9-x"),
            parse("https://example.com/invite/Ab_9-x", appLinkHost = "example.com")
        )
        assertNull(parse("https://example.com/invite/Ab_9-x"))
        assertNull(parse("myapp://invite/abc/accept"))
    }

    // The server issues base64url tokens: letters, digits, "-" and "_"
    @Test
    fun aBase64UrlTokenIsAccepted() {
        assertEquals(InviteDestination("aZ09-_xY"), parse("myapp://invite/aZ09-_xY"))
    }

    @Test
    fun inviteUrlBuildsTheLinkTheRouteOpens() {
        val url = inviteUrl(scheme = "myapp", token = "abc")

        assertEquals("myapp://invite/abc", url)
        assertEquals(InviteDestination("abc"), parse(url))
    }

    @Test
    fun anEmptyTokenIsRejected() {
        assertNull(parse("myapp://invite/"))
        assertNull(parse("myapp://invite"))
    }

    // A "/" inside the token: as a second segment, or percent-encoded into the one segment
    @Test
    fun aTokenWithASlashIsRejected() {
        assertNull(parse("myapp://invite/ab/cd"))
        assertNull(parse("myapp://invite/ab%2Fcd"))
    }

    @Test
    fun aTokenOutsideBase64UrlIsRejected() {
        assertNull(parse("myapp://invite/ab.cd"))
        assertNull(parse("myapp://invite/ab%20cd"))
        assertNull(parse("myapp://invite/ab+cd"))
        assertNull(parse("myapp://invite/%C3%A7"))
    }
}
