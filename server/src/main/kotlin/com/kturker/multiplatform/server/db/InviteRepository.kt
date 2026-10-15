package com.kturker.multiplatform.server.db

import com.kturker.multiplatform.core.api.InviteLink
import com.kturker.multiplatform.core.api.InvitePreview
import com.kturker.multiplatform.core.api.InviteState
import com.kturker.multiplatform.core.api.Role
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.UUID

/** How long an invite link stays valid: seven days from its creation. */
const val INVITE_TTL_MS = 7L * 24 * 60 * 60 * 1000

private const val TOKEN_BYTES = 32

sealed interface AcceptResult {
    data class Joined(val spaceId: UUID) : AcceptResult
    data object Used : AcceptResult
    data object Expired : AcceptResult
    data object NotFound : AcceptResult
}

/**
 * Single-use invite links. The token (32 random bytes, URL-safe base64) is handed out once; only its SHA-256 hex
 * digest is stored, and every lookup goes through that digest.
 */
class InviteRepository(
    private val db: Database,
    private val now: () -> Long = System::currentTimeMillis,
    private val random: SecureRandom = SecureRandom()
) {
    /** A new invite to [spaceId] with [role] (EDITOR or VIEWER; the schema refuses OWNER). */
    fun create(
        spaceId: UUID,
        createdBy: String,
        role: Role
    ): InviteLink {
        require(role != Role.OWNER) { "Owner cannot be invited" }
        val bytes = ByteArray(TOKEN_BYTES).also(random::nextBytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        val createdAt = now()
        val expiresAt = createdAt + INVITE_TTL_MS
        transaction(db) {
            Invites.insert {
                it[id] = UUID.randomUUID()
                it[Invites.spaceId] = spaceId
                it[tokenHash] = hash(token)
                it[Invites.role] = role.wire()
                it[Invites.createdBy] = createdBy
                it[Invites.createdAt] = createdAt
                it[Invites.expiresAt] = expiresAt
            }
        }
        return InviteLink(token, expiresAt)
    }

    /** What the invite offers and whether it can still be used, with its space id; null for an unknown token. */
    fun preview(token: String): Pair<InvitePreview, UUID>? = transaction(db) {
        Invites.join(Spaces, JoinType.INNER, Invites.spaceId, Spaces.id)
            .join(Users, JoinType.LEFT, Invites.createdBy, Users.uid)
            .selectAll()
            .where { Invites.tokenHash eq hash(token) }
            .singleOrNull()
            ?.let { row ->
                val preview = InvitePreview(
                    spaceName = row[Spaces.name],
                    inviterName = row[Users.displayName],
                    role = roleOf(row[Invites.role]),
                    state = row.state()
                )
                preview to row[Invites.spaceId]
            }
    }

    /**
     * Joins [uid] to the invite's space, in one transaction with the invite row locked (`FOR UPDATE`), so two
     * concurrent accepts cannot both use it. A caller who already is a member keeps their role and the invite stays
     * unused.
     */
    fun accept(
        token: String,
        uid: String
    ): AcceptResult = transaction(db) {
        val row = Invites.selectAll().where { Invites.tokenHash eq hash(token) }.forUpdate().singleOrNull()
            ?: return@transaction AcceptResult.NotFound
        val spaceId = row[Invites.spaceId]
        val isMember = !SpaceMembers.selectAll()
            .where { (SpaceMembers.spaceId eq spaceId) and (SpaceMembers.uid eq uid) }
            .empty()
        when {
            isMember -> AcceptResult.Joined(spaceId)

            row.state() == InviteState.USED -> AcceptResult.Used

            row.state() == InviteState.EXPIRED -> AcceptResult.Expired

            else -> {
                val at = now()
                SpaceMembers.insertIgnore {
                    it[SpaceMembers.spaceId] = spaceId
                    it[SpaceMembers.uid] = uid
                    it[role] = row[Invites.role]
                    it[joinedAt] = at
                }
                Invites.update({ Invites.id eq row[Invites.id] }) {
                    it[usedBy] = uid
                    it[usedAt] = at
                }
                AcceptResult.Joined(spaceId)
            }
        }
    }

    /** Removes every invite [uid] created (account deletion). */
    fun deleteCreatedBy(uid: String) {
        transaction(db) { Invites.deleteWhere { createdBy eq uid } }
    }

    // used_at, not used_by: used_by is cleared when the user who used it deletes their account.
    private fun ResultRow.state(): InviteState = when {
        this[Invites.usedAt] != null -> InviteState.USED
        this[Invites.expiresAt] <= now() -> InviteState.EXPIRED
        else -> InviteState.VALID
    }

    private fun hash(token: String): String =
        MessageDigest.getInstance("SHA-256").digest(token.toByteArray()).joinToString("") { "%02x".format(it) }
}
