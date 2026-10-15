package com.kturker.multiplatform.server.db

import com.kturker.multiplatform.core.api.Role
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

/** The personal space's name until the client sends its localized one (`PUT /me/profile`). */
const val DEFAULT_PERSONAL_SPACE_NAME = "Personal"

class UserRepository(private val db: Database) {
    /**
     * Makes sure the caller exists, in one transaction: the user row, and a personal space with the caller as its
     * owner member. Email follows the token; the token's name fills the display name only while none is stored
     * (one set with [setDisplayName] wins). A claim the token lacks keeps the stored value.
     *
     * Safe under concurrent first requests: both inserts are `ON CONFLICT DO NOTHING`, and the partial unique index
     * `spaces_one_personal` lets only one personal space per user through.
     */
    fun touch(
        uid: String,
        email: String?,
        name: String?
    ) = transaction(db) {
        val now = System.currentTimeMillis()
        val created = Users.insertIgnore {
            it[Users.uid] = uid
            it[Users.email] = email
            it[displayName] = name
            it[createdAt] = now
        }.insertedCount > 0
        if (!created) updateClaims(uid, email, name)
        ensurePersonalSpace(uid, now)
    }

    /** Sets the name the caller chose in the app; from then on the token's `name` claim no longer applies. */
    fun setDisplayName(
        uid: String,
        name: String
    ) = transaction(db) {
        Users.update({ Users.uid eq uid }) { it[displayName] = name }
    }

    /**
     * Removes the user row; the schema's ON DELETE CASCADE removes whatever still points at it (memberships in other
     * people's spaces, and anything account deletion did not already remove). A user that does not exist is a no-op.
     */
    fun delete(uid: String) {
        transaction(db) { Users.deleteWhere { Users.uid eq uid } }
    }

    // The token's name only fills an empty display name: a stored one may have been chosen in the app.
    private fun updateClaims(
        uid: String,
        email: String?,
        name: String?
    ) {
        if (email == null && name == null) return
        val row = Users.selectAll().where { Users.uid eq uid }.single()
        val newEmail = email ?: row[Users.email]
        val newName = row[Users.displayName] ?: name
        if (newEmail == row[Users.email] && newName == row[Users.displayName]) return
        Users.update({ Users.uid eq uid }) {
            it[Users.email] = newEmail
            it[displayName] = newName
        }
    }

    private fun ensurePersonalSpace(
        uid: String,
        now: Long
    ) {
        val hasPersonal = !Spaces.selectAll()
            .where { (Spaces.ownerUid eq uid) and (Spaces.isPersonal eq true) }
            .empty()
        if (hasPersonal) return
        val spaceId = UUID.randomUUID()
        val inserted = Spaces.insertIgnore {
            it[id] = spaceId
            it[ownerUid] = uid
            it[name] = DEFAULT_PERSONAL_SPACE_NAME
            it[isPersonal] = true
            it[createdAt] = now
        }.insertedCount > 0
        if (!inserted) return // a concurrent first request created it
        SpaceMembers.insert {
            it[SpaceMembers.spaceId] = spaceId
            it[SpaceMembers.uid] = uid
            it[role] = Role.OWNER.wire()
            it[joinedAt] = now
        }
    }
}
