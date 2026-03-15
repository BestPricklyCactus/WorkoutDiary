package ru.pricklycactus.workoutdiary.feature.editor

import ru.pricklycactus.workoutdiary.core.mvi.MviEffect
import ru.pricklycactus.workoutdiary.core.mvi.MviIntent

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
