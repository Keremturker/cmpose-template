package com.kturker.multiplatform.feature.notes.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun NotesScreenRoute(viewModel: NotesViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotesScreen(uiState = uiState, actions = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesScreen(
    uiState: NotesUiState,
    actions: NotesActions
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notes") },
                navigationIcon = {
                    TextButton(onClick = actions::onNavigateUp) {
                        Text("Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Row {
                OutlinedTextField(
                    value = uiState.text,
                    onValueChange = actions::onTextChange,
                    placeholder = { Text("New note") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = actions::onAddClick, enabled = uiState.text.isNotBlank()) {
                    Text("Add")
                }
            }
            if (uiState.notes.isEmpty()) {
                Text("No notes yet")
            } else {
                LazyColumn {
                    items(uiState.notes, key = { it.id }) { note ->
                        Row {
                            Text(note.text, modifier = Modifier.weight(1f))
                            TextButton(onClick = { actions.onDeleteClick(note.id) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
