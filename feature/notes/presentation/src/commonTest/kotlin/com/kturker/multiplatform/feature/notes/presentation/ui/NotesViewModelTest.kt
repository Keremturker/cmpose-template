package com.kturker.multiplatform.feature.notes.presentation.ui

import androidx.lifecycle.viewModelScope
import com.kturker.multiplatform.core.navigation.NavigationCommand
import com.kturker.multiplatform.core.navigation.NavigationManager
import com.kturker.multiplatform.core.room.notes.NoteDao
import com.kturker.multiplatform.core.room.notes.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// viewModelScope runs on Dispatchers.Main, which the tests swap for a test dispatcher
@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private val noteDao = FakeNoteDao()
    private val createdViewModels = mutableListOf<NotesViewModel>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    // Every view model's scope is cancelled while Main is still the test dispatcher
    @AfterTest
    fun tearDown() = runTest {
        createdViewModels.forEach { it.viewModelScope.coroutineContext.job.cancelAndJoin() }
        createdViewModels.clear()
        Dispatchers.resetMain()
    }

    private fun createViewModel() = NotesViewModel(noteDao, RecordingNavigationManager())
        .also { createdViewModels += it }

    @Test
    fun notesAreListedLive() = runTest {
        val viewModel = createViewModel()

        noteDao.insert(note = NoteEntity(text = "Buy milk", createdAt = 1))

        assertEquals(1, viewModel.uiState.first { it.notes.size == 1 }.notes.size)
    }

    @Test
    fun addingSavesTheTrimmedTextAndClearsTheField() = runTest {
        val viewModel = createViewModel()

        viewModel.onTextChange(text = "  Buy milk ")
        viewModel.onAddClick()

        assertEquals(listOf("Buy milk"), noteDao.notes.value.map { it.text })
        assertEquals("", viewModel.uiState.value.text)
    }

    @Test
    fun blankTextIsNotSaved() = runTest {
        val viewModel = createViewModel()

        viewModel.onTextChange(text = "   ")
        viewModel.onAddClick()

        assertTrue(noteDao.notes.value.isEmpty())
    }

    @Test
    fun deletingRemovesTheNote() = runTest {
        noteDao.insert(note = NoteEntity(text = "One", createdAt = 1))
        noteDao.insert(note = NoteEntity(text = "Two", createdAt = 2))
        val viewModel = createViewModel()

        viewModel.onDeleteClick(id = 1)

        assertEquals(listOf(2L), viewModel.uiState.first { it.notes.size == 1 }.notes.map { it.id })
    }
}

private class FakeNoteDao : NoteDao {
    val notes = MutableStateFlow<List<NoteEntity>>(listOf())
    private var lastId = 0L

    override fun observeAll(): Flow<List<NoteEntity>> = notes

    override suspend fun insert(note: NoteEntity): Long {
        lastId += 1
        notes.value += note.copy(id = lastId)
        return lastId
    }

    override suspend fun deleteById(id: Long) {
        notes.value = notes.value.filterNot { it.id == id }
    }
}

private class RecordingNavigationManager : NavigationManager {
    override val navigationCommandFlow: Flow<NavigationCommand> = emptyFlow()

    override fun navigate(navigationCommand: NavigationCommand) = Unit
}
