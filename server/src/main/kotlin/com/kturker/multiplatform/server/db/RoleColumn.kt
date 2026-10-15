package com.kturker.multiplatform.server.db

import com.kturker.multiplatform.core.api.Role

/** The value stored in a `role` column; the same names the API uses on the wire. */
fun Role.wire(): String = when (this) {
    Role.OWNER -> "owner"
    Role.EDITOR -> "editor"
    Role.VIEWER -> "viewer"
}

/** The [Role] for a stored `role` value; the schema's CHECK constraint allows nothing else. */
fun roleOf(wire: String): Role = Role.entries.firstOrNull { it.wire() == wire }
    ?: throw IllegalArgumentException("Unknown role '$wire'")
