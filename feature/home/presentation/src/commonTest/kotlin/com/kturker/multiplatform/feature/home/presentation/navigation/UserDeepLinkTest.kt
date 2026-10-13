package com.kturker.multiplatform.feature.home.presentation.navigation

import com.kturker.multiplatform.feature.home.contract.HomeScreenDestination
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserDeepLinkTest {

    private val route = UserDeepLink()

    @Test
    fun aGithubUsernameOpensItsProfile() {
        assertEquals(
            HomeScreenDestination(githubUsername = "octocat"),
            route.destination(mapOf("username" to "octocat"))
        )
    }

    @Test
    fun anInvalidUsernameOpensNothing() {
        assertNull(route.destination(mapOf("username" to "octo_cat!")))
        assertNull(route.destination(mapOf("username" to "-octocat")))
        assertNull(route.destination(mapOf("username" to "a".repeat(40))))
    }
}
