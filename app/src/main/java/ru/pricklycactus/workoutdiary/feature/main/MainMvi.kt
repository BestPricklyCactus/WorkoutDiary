package ru.pricklycactus.workoutdiary.feature.main

import ru.pricklycactus.workoutdiary.core.mvi.MviEffect
import ru.pricklycactus.workoutdiary.core.mvi.MviIntent

sealed interface MainIntent : MviIntent {
    data class OnClick(val action: String) : MainIntent
    data class OnTextChanged(val field: String, val text: String) : MainIntent
    data class OnExerciseSelected(val exerciseId: Long, val selected: Boolean) : MainIntent
    data class OnExercisesDelete(val exerciseIds: Set<Long>) : MainIntent
    data object NavigateToWorkout : MainIntent
}

sealed interface MainEffect : MviEffect {
    data object NavigateToWorkout : MainEffect
}
