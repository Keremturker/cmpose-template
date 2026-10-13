package com.kturker.multiplatform.core.room.notes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Used by blank's Notes demo (feature/notes); the showcase's database does not list NoteEntity
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
