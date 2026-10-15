package com.kturker.multiplatform.server

import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.server.db.SpaceMembers
import com.kturker.multiplatform.server.db.Spaces
import com.kturker.multiplatform.server.db.UserRepository
import com.kturker.multiplatform.server.db.Users
import com.kturker.multiplatform.server.db.roleOf
import com.kturker.multiplatform.server.db.wire
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserRepositoryTest {
    @BeforeTest
    fun clean() {
        TestDb.reset()
    }

    /** Runs [threads] `touch` calls for the same user at once and waits for all of them. */
    private fun raceTouch(threads: Int = 8) {
        val users = UserRepository(TestDb.database)
        val start = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(threads)
        try {
            val results = (1..threads).map {
                pool.submit {
                    start.await()
                    users.touch("alice", "alice@example.com", null)
                }
            }
            start.countDown()
            results.forEach { it.get(30, TimeUnit.SECONDS) }
        } finally {
            pool.shutdownNow()
        }
    }

    private fun assertOnePersonalSpace() = transaction(TestDb.database) {
        assertEquals(1L, Spaces.selectAll().count())
        assertEquals(1L, SpaceMembers.selectAll().count())
    }

    @Test
    fun `concurrent first requests create exactly one personal space`() {
        raceTouch()
        assertOnePersonalSpace()
    }

    @Test
    fun `concurrent requests for an existing user without a space create exactly one`() {
        // The user row already exists, so no request waits on the users insert: they all race on the spaces insert
        transaction(TestDb.database) {
            Users.insert {
                it[uid] = "alice"
                it[email] = "alice@example.com"
                it[createdAt] = 0L
            }
        }
        raceTouch()
        assertOnePersonalSpace()
    }

    @Test
    fun `roles round trip through their stored names`() {
        Role.entries.forEach { assertEquals(it, roleOf(it.wire())) }
        assertEquals(listOf("owner", "editor", "viewer"), Role.entries.map { it.wire() })
        assertFailsWith<IllegalArgumentException> { roleOf("admin") }
    }
}
