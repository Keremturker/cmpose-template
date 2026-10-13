package com.kturker.multiplatform.core.multilang.resource

import com.kturker.multiplatform.core.domain.DataError
import com.kturker.multiplatform.core.multilang.resource.res.fallbackResources
import com.kturker.multiplatform.core.multilang.resource.res.languageResources
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ErrorMessageTest {

    private val strings = fallbackResources

    @Test
    fun noConnectionHasItsOwnMessage() {
        assertEquals(strings.errorNoConnection, strings.errorMessage(error = DataError.Network))
    }

    // GitHub answers 403 (primary) or 429 (secondary) once its request limit is used up
    @Test
    fun rateLimitStatusesShowTheRateLimitMessage() {
        assertEquals(strings.errorRateLimit, strings.errorMessage(error = DataError.Http(code = 403)))
        assertEquals(strings.errorRateLimit, strings.errorMessage(error = DataError.Http(code = 429)))
    }

    @Test
    fun otherStatusesShowTheServerMessage() {
        listOf(400, 404, 500, 503).forEach { code ->
            assertEquals(strings.errorServer, strings.errorMessage(error = DataError.Http(code = code)), "status $code")
        }
    }

    @Test
    fun unreadableBodiesAndUnknownFailuresShowTheUnexpectedMessage() {
        assertEquals(strings.errorUnexpected, strings.errorMessage(error = DataError.Serialization))
        assertEquals(strings.errorUnexpected, strings.errorMessage(error = DataError.Unknown(IllegalStateException())))
    }

    @Test
    fun everyLanguageHasFourDistinctMessagesAndARetryLabel() {
        languageResources.forEach { (language, resources) ->
            val messages = setOf(
                resources.errorNoConnection,
                resources.errorRateLimit,
                resources.errorServer,
                resources.errorUnexpected
            )
            assertEquals(4, messages.size, "$language")
            assertTrue(messages.none { it.isBlank() } && resources.retry.isNotBlank(), "$language")
        }
    }
}
