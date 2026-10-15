package com.kturker.multiplatform.feature.sharing.contract

import com.kturker.multiplatform.core.api.Space
import kotlinx.coroutines.flow.StateFlow

/** The space the app's own screens show; persisted (DataStore), defaults to the personal space. */
interface SelectedSpace {
    /** Null until the first [refresh] answers, and after [clear] (while signed out). */
    val current: StateFlow<Space?>

    /** Selects one of the user's spaces and remembers it. */
    suspend fun select(spaceId: String)

    /** Reloads GET /spaces; keeps the selection if still a member, else the personal space. */
    suspend fun refresh()

    /** Forgets the selection, shown and stored: the app shell calls it when the user signs out or changes. */
    suspend fun clear()
}
