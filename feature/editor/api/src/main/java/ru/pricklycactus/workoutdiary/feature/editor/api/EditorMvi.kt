package ru.pricklycactus.workoutdiary.feature.editor.api

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import ru.pricklycactus.workoutdiary.core.mvi.MviIntent
import ru.pricklycactus.workoutdiary.core.mvi.MviEffect

interface EditorStore {
    val state: StateFlow<EditorViewState>
    val effect: Flow<EditorEffect>
    fun dispatch(intent: EditorIntent)
}

sealed interface EditorIntent : MviIntent {
    data object AddExerciseClick : EditorIntent
    data object CancelAddExerciseClick : EditorIntent
    data object SaveExerciseClick : EditorIntent
    data class OnTextChanged(val field: String, val text: String) : EditorIntent
    data class OnExerciseSelected(val exerciseId: Long, val selected: Boolean) : EditorIntent
    data class OnExercisesDelete(val exerciseIds: Set<Long>) : EditorIntent
    data object LoadExercises : EditorIntent
}

sealed interface EditorEffect : MviEffect
