package com.kturker.multiplatform.core.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepLinkParserTest {

    private data class Profile(val name: String) : NavigationCommand.Destination
    private data object Preferences : NavigationCommand.Destination

    private val routes = listOf(
        route(pattern = "user/{username}") { Profile(it.getValue("username")) },
        route(pattern = "settings") { Preferences }
    )

    private fun parse(
        uri: String,
        appLinkHost: String = ""
    ) = parseDeepLink(uri = uri, scheme = "myapp", appLinkHost = appLinkHost, routes = routes)

    @Test
    fun aParameterIsReadFromItsSegment() {
        assertEquals(Profile("octocat"), parse("myapp://user/octocat"))
    }

    // The same path through the custom scheme or, once APP_LINK_HOST is set, an https App/Universal Link
    @Test
    fun aFixedPathOpensItsScreen() {
        assertEquals(Preferences, parse("myapp://settings"))
        assertEquals(Preferences, parse("https://example.com/settings", appLinkHost = "example.com"))
    }

    @Test
    fun otherSchemesAndHostsOpenNothing() {
        assertNull(parse("otherapp://settings"))
        assertNull(parse("https://example.com/settings"))
        assertNull(parse("https://evil.example/settings", appLinkHost = "example.com"))
    }

    @Test
    fun anUnknownOrEmptyPathOpensNothing() {
        assertNull(parse("myapp://orders"))
        assertNull(parse("myapp://"))
    }

    @Test
    fun aMissingParameterOpensNothing() {
        assertNull(parse("myapp://user"))
        assertNull(parse("myapp://user/"))
    }

    @Test
    fun anExtraSegmentOpensNothing() {
        assertNull(parse("myapp://user/octocat/repos"))
    }

    // Scheme case does not matter, the query and fragment are ignored, a parameter is percent-decoded
    @Test
    fun linksAreNormalised() {
        assertEquals(Preferences, parse("MyApp://settings"))
        assertEquals(Profile("octo-cat"), parse("myapp://user/octo%2Dcat?ref=mail#top"))
    }
}

private fun route(
    pattern: String,
    destination: (Map<String, String>) -> NavigationCommand.Destination?
) = object : DeepLinkRoute {
    override val pattern: String = pattern
    override fun destination(arguments: Map<String, String>) = destination(arguments)
}
