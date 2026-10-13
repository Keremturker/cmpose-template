package com.kturker.multiplatform.feature.notes.presentation.ui

internal interface NotesActions {
    fun onTextChange(text: String)
    fun onAddClick()
    fun onDeleteClick(id: Long)
    fun onNavigateUp()
}
