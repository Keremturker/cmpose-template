package com.kturker.multiplatform.server.db

import com.kturker.multiplatform.core.api.Member
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.core.api.Space
import com.kturker.multiplatform.server.access.Membership
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

/** Longest space name (and personal space name) after trimming. */
const val MAX_SPACE_NAME = 80

/** Spaces and their members. Callers check roles first (`requireRole`); nothing here checks who is asking. */
class SpaceRepository(private val db: Database) {
    fun membership(
        spaceId: UUID,
        uid: String
    ): Membership? = transaction(db) {
        SpaceMembers.selectAll()
            .where { (SpaceMembers.spaceId eq spaceId) and (SpaceMembers.uid eq uid) }
            .singleOrNull()
            ?.let { Membership(spaceId, uid, roleOf(it[SpaceMembers.role])) }
    }

    /** The spaces [uid] belongs to, with their role and member count: their own personal space first, then by name. */
    fun listFor(uid: String): List<Space> = transaction(db) {
        val rows = (SpaceMembers innerJoin Spaces).selectAll().where { SpaceMembers.uid eq uid }.toList()
        val counts = memberCounts(rows.map { it[Spaces.id] })
        rows.sortedWith(
            compareByDescending<ResultRow> { it.isOwnPersonal(uid) }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it[Spaces.name] }
                .thenBy { it[Spaces.id] }
        ).map { it.toSpace(counts[it[Spaces.id]] ?: 1) }
    }

    /** The space as [uid] sees it, or null when they are not a member. */
    fun get(
        spaceId: UUID,
        uid: String
    ): Space? = transaction(db) {
        val row = (SpaceMembers innerJoin Spaces).selectAll()
            .where { (SpaceMembers.spaceId eq spaceId) and (SpaceMembers.uid eq uid) }
            .singleOrNull()
        row?.toSpace(memberCounts(listOf(spaceId))[spaceId] ?: 1)
    }

    /** The ids of the spaces [uid] owns, their personal space included. */
    fun ownedBy(uid: String): List<UUID> = transaction(db) {
        Spaces.select(Spaces.id).where { Spaces.ownerUid eq uid }.map { it[Spaces.id] }
    }

    fun isPersonal(spaceId: UUID): Boolean = transaction(db) {
        Spaces.selectAll().where { Spaces.id eq spaceId }.singleOrNull()?.get(Spaces.isPersonal) ?: false
    }

    /** A new shared space; [uid] becomes its OWNER. */
    fun create(
        uid: String,
        name: String
    ): Space = transaction(db) {
        val now = System.currentTimeMillis()
        val spaceId = UUID.randomUUID()
        Spaces.insert {
            it[id] = spaceId
            it[ownerUid] = uid
            it[Spaces.name] = name
            it[isPersonal] = false
            it[createdAt] = now
        }
        SpaceMembers.insert {
            it[SpaceMembers.spaceId] = spaceId
            it[SpaceMembers.uid] = uid
            it[role] = Role.OWNER.wire()
            it[joinedAt] = now
        }
        Space(spaceId.toString(), name, Role.OWNER, isPersonal = false, memberCount = 1)
    }

    fun rename(
        spaceId: UUID,
        name: String
    ) {
        transaction(db) { Spaces.update({ Spaces.id eq spaceId }) { it[Spaces.name] = name } }
    }

    /**
     * Deletes the space; its members, invites and file rows go with it (ON DELETE CASCADE). Returns the stored paths
     * of its files, for the caller to remove from disk.
     */
    fun delete(spaceId: UUID): List<String> = transaction(db) {
        val paths = Files.selectAll().where { Files.spaceId eq spaceId }.map { it[Files.path] }
        Spaces.deleteWhere { id eq spaceId }
        paths
    }

    /** Members with their profile: the owner first, then in the order they joined. */
    fun members(spaceId: UUID): List<Member> = transaction(db) {
        (SpaceMembers innerJoin Users).selectAll()
            .where { SpaceMembers.spaceId eq spaceId }
            .map {
                Member(
                    uid = it[SpaceMembers.uid],
                    displayName = it[Users.displayName],
                    email = it[Users.email],
                    role = roleOf(it[SpaceMembers.role]),
                    joinedAt = it[SpaceMembers.joinedAt]
                )
            }
            .sortedWith(compareByDescending<Member> { it.role == Role.OWNER }.thenBy { it.joinedAt }.thenBy { it.uid })
    }

    /** False when [uid] is not a member. */
    fun setRole(
        spaceId: UUID,
        uid: String,
        role: Role
    ): Boolean = transaction(db) {
        SpaceMembers.update({ (SpaceMembers.spaceId eq spaceId) and (SpaceMembers.uid eq uid) }) {
            it[SpaceMembers.role] = role.wire()
        } > 0
    }

    /** False when [uid] is not a member. */
    fun removeMember(
        spaceId: UUID,
        uid: String
    ): Boolean = transaction(db) {
        SpaceMembers.deleteWhere { (SpaceMembers.spaceId eq spaceId) and (SpaceMembers.uid eq uid) } > 0
    }

    /** Adds [uid] with [role]; a no-op when they already are a member (their role stays). */
    fun addMember(
        spaceId: UUID,
        uid: String,
        role: Role
    ) {
        transaction(db) {
            SpaceMembers.insertIgnore {
                it[SpaceMembers.spaceId] = spaceId
                it[SpaceMembers.uid] = uid
                it[SpaceMembers.role] = role.wire()
                it[joinedAt] = System.currentTimeMillis()
            }
        }
    }

    /**
     * Gives [uid]'s personal space its localized [name], but only while it still has the server's default name
     * ([DEFAULT_PERSONAL_SPACE_NAME]): a name the owner chose stays, however often the client syncs the profile.
     */
    fun localizePersonal(
        uid: String,
        name: String
    ) {
        transaction(db) {
            Spaces.update({
                (Spaces.ownerUid eq uid) and
                    (Spaces.isPersonal eq true) and
                    (Spaces.name eq DEFAULT_PERSONAL_SPACE_NAME)
            }) { it[Spaces.name] = name }
        }
    }

    private fun memberCounts(spaceIds: List<UUID>): Map<UUID, Int> {
        if (spaceIds.isEmpty()) return emptyMap()
        val count = SpaceMembers.uid.count()
        return SpaceMembers.select(SpaceMembers.spaceId, count)
            .where { SpaceMembers.spaceId inList spaceIds }
            .groupBy(SpaceMembers.spaceId)
            .associate { it[SpaceMembers.spaceId] to it[count].toInt() }
    }

    private fun ResultRow.isOwnPersonal(uid: String) =
        this[Spaces.isPersonal] && this[Spaces.ownerUid] == uid

    private fun ResultRow.toSpace(memberCount: Int) = Space(
        id = this[Spaces.id].toString(),
        name = this[Spaces.name],
        role = roleOf(this[SpaceMembers.role]),
        isPersonal = this[Spaces.isPersonal],
        memberCount = memberCount
    )
}
