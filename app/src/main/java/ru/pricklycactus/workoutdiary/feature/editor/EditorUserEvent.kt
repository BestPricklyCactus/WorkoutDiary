package ru.pricklycactus.workoutdiary.feature.editor

sealed class EditorUserEvent {
    data object AddExerciseClick : EditorUserEvent()
    data object CancelAddExerciseClick : EditorUserEvent()
    data object SaveExerciseClick : EditorUserEvent()
    data class OnTextChanged(val field: String, val text: String) : EditorUserEvent()
    data class OnExerciseSelected(val exerciseId: Long, val selected: Boolean) : EditorUserEvent()
    data class OnExercisesDelete(val exerciseIds: Set<Long>) : EditorUserEvent()
    data object LoadExercises : EditorUserEvent()
}
