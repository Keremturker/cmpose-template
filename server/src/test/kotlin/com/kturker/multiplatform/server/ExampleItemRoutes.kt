package com.kturker.multiplatform.server

import com.kturker.multiplatform.core.api.ApiRoutes
import com.kturker.multiplatform.core.api.Role
import com.kturker.multiplatform.server.access.requireRole
import com.kturker.multiplatform.server.db.SpaceRepository
import com.kturker.multiplatform.server.db.Spaces
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

/**
 * REFERENCE PATTERN for app-specific data (test sources only; nothing here ships).
 *
 * Copy this shape when adding a feature's own records to the server:
 *  1. A table with a `space_id` column referencing `spaces(id) ON DELETE CASCADE`. In the app, the table comes from
 *     a Flyway migration (`db/migration/V2__<feature>.sql`, ...) and the Exposed object below only describes it; the
 *     test creates it with SchemaUtils because it is not part of the real schema.
 *  2. Routes under `/spaces/{id}/...`, mounted inside the `authenticate(FIREBASE_AUTH)` block in `module()`.
 *  3. Every handler starts with `call.requireRole(spaces, call.parameters["id"].orEmpty(), <role>)`: VIEWER to read,
 *     EDITOR to write. It answers 404 for a non-member or a weaker role, so never check membership by hand and never
 *     answer 403. Queries then use the returned `membership.spaceId`, never the raw path value.
 *  4. Deleting a space cascades to these rows; files stored for them go through the `files` table so that space
 *     deletion can remove them from disk.
 *
 * [ExampleItems] is the example record: it belongs to one space.
 */
object ExampleItems : Table("example_items") {
    val id = uuid("id")
    val spaceId = uuid("space_id").references(Spaces.id, onDelete = ReferenceOption.CASCADE)
    val title = text("title")
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class ExampleItem(val id: String, val title: String)

@Serializable
data class ExampleItemDraft(val title: String)

/** `GET` (VIEWER) and `POST` (EDITOR) on `/spaces/{id}/items`. */
fun Route.exampleItemRoutes(spaces: SpaceRepository, db: Database) {
    route("/" + ApiRoutes.space("{id}") + "/items") {
        get {
            val membership = call.requireRole(spaces, call.parameters["id"].orEmpty(), Role.VIEWER)
            val items = transaction(db) {
                ExampleItems.selectAll().where { ExampleItems.spaceId eq membership.spaceId }
                    .map { ExampleItem(it[ExampleItems.id].toString(), it[ExampleItems.title]) }
            }
            call.respond(items)
        }
        post {
            val membership = call.requireRole(spaces, call.parameters["id"].orEmpty(), Role.EDITOR)
            val draft = call.receive<ExampleItemDraft>()
            val itemId = UUID.randomUUID()
            transaction(db) {
                ExampleItems.insert {
                    it[id] = itemId
                    it[spaceId] = membership.spaceId
                    it[title] = draft.title
                }
            }
            call.respond(HttpStatusCode.Created, ExampleItem(itemId.toString(), draft.title))
        }
    }
}
