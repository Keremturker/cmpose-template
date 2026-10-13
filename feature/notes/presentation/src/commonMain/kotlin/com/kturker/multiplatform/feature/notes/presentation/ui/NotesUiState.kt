package com.kturker.multiplatform.feature.notes.presentation.ui

import com.kturker.multiplatform.core.room.notes.NoteEntity

internal data class NotesUiState(val text: String = "", val notes: List<NoteEntity> = listOf())
