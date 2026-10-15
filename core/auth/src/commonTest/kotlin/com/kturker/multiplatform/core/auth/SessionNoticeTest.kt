package com.kturker.multiplatform.core.auth

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SessionNoticeTest {

    @Test
    fun aPostedNoticeIsTakenOnce() {
        val notice = SessionNotice()
        notice.post()

        assertTrue(notice.take())
        assertFalse(notice.take())
    }

    @Test
    fun nothingPostedNothingTaken() {
        assertFalse(SessionNotice().take())
    }
}
