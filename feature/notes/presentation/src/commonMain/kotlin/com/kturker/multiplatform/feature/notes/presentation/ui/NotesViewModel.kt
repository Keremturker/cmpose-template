package com.kturker.multiplatform.feature.notes.presentation.ui

import androidx.lifecycle.viewModelScope
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.core.navigation.NavigationManager
import com.kturker.multiplatform.core.presentation.CoreViewModel
import com.kturker.multiplatform.core.room.notes.NoteDao
import com.kturker.multiplatform.core.room.notes.NoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Provided
import kotlin.time.Clock

// Kept short on purpose: the ViewModel talks to NoteDao directly. In a real feature, put a repository in
// a data layer and keep Room out of the presentation layer.
@KoinViewModel
internal class NotesViewModel(
    @Provided private val noteDao: NoteDao,
    @Provided private val navigationManager: NavigationManager
) : CoreViewModel(),
    NotesActions {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        noteDao.observeAll()
            .onEach { notes -> _uiState.update { it.copy(notes = notes) } }
            .launchIn(viewModelScope)
    }

    override fun onTextChange(text: String) = _uiState.update { it.copy(text = text) }

    override fun onAddClick() {
        val text = _uiState.value.text.trim()
        if (text.isEmpty()) return
        _uiState.update { it.copy(text = "") }
        viewModelScope.launch {
            noteDao.insert(note = NoteEntity(text = text, createdAt = Clock.System.now().toEpochMilliseconds()))
        }
    }

    override fun onDeleteClick(id: Long) {
        viewModelScope.launch { noteDao.deleteById(id = id) }
    }

    override fun onNavigateUp() = navigationManager.navigate(NavigationCommand.NavigateUp)
}
