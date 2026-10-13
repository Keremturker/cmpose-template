package com.kturker.multiplatform.core.room

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.kturker.multiplatform.core.room.notes.NoteDao
import com.kturker.multiplatform.core.room.notes.NoteEntity
import org.koin.core.annotation.Single

// The app's Room database. To add a table: an @Entity and a @Dao in their own package, the entity in
// `entities`, an abstract function for the DAO and a @Single provider below. Changing a table needs a
// new `version` and a migration; core/room/schemas/ keeps every version's schema (commit it).
@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = true
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}

// Room generates the actual object for each platform (KSP)
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

@Single
fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()
