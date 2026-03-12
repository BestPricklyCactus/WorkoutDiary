package ru.pricklycactus.workoutdiary.feature.main

sealed class MainUserEvent {
    data class OnClick(val action: String) : MainUserEvent()
    data class OnTextChanged(val field: String, val text: String) : MainUserEvent()
    data class OnExerciseSelected(val exerciseId: Long, val selected: Boolean) : MainUserEvent()
    data class OnExercisesDelete(val exerciseIds: Set<Long>) : MainUserEvent()
    data class OnExercisesLoaded(val exerciseCount: Int) : MainUserEvent()
}
