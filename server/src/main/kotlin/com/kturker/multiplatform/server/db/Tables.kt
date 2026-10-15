package com.kturker.multiplatform.server.db

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

// Mirrors db/migration/V1__core.sql; Flyway owns the schema, these objects only describe it to Exposed.

object Users : Table("users") {
    val uid = text("uid")
    val email = text("email").nullable()
    val displayName = text("display_name").nullable()
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(uid)
}

object Spaces : Table("spaces") {
    val id = uuid("id")
    val ownerUid = text("owner_uid").references(Users.uid, onDelete = ReferenceOption.CASCADE)
    val name = text("name")
    val isPersonal = bool("is_personal")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

/** `role` holds the wire name of a [com.kturker.multiplatform.core.api.Role]; see RoleColumn.kt. */
object SpaceMembers : Table("space_members") {
    val spaceId = uuid("space_id").references(Spaces.id, onDelete = ReferenceOption.CASCADE)
    val uid = text("uid").references(Users.uid, onDelete = ReferenceOption.CASCADE)
    val role = text("role")
    val joinedAt = long("joined_at")
    override val primaryKey = PrimaryKey(spaceId, uid)
}

object Invites : Table("invites") {
    val id = uuid("id")
    val spaceId = uuid("space_id").references(Spaces.id, onDelete = ReferenceOption.CASCADE)
    val tokenHash = text("token_hash")
    val role = text("role")
    val createdBy = text("created_by").references(Users.uid, onDelete = ReferenceOption.CASCADE)
    val createdAt = long("created_at")
    val expiresAt = long("expires_at")
    val usedBy = text("used_by").references(Users.uid, onDelete = ReferenceOption.SET_NULL).nullable()
    val usedAt = long("used_at").nullable()
    override val primaryKey = PrimaryKey(id)
}

object Files : Table("files") {
    val id = uuid("id")
    val spaceId = uuid("space_id").references(Spaces.id, onDelete = ReferenceOption.CASCADE)
    val uploadedBy = text("uploaded_by").references(Users.uid, onDelete = ReferenceOption.SET_NULL).nullable()
    val path = text("path")
    val mime = text("mime")
    val size = long("size")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}
