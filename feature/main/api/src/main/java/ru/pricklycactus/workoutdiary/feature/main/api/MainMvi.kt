package ru.pricklycactus.workoutdiary.feature.main.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.pricklycactus.workoutdiary.core.mvi.MviEffect
import ru.pricklycactus.workoutdiary.core.mvi.MviIntent

interface MainStore {
    val state: StateFlow<MainViewState>
    val effect: Flow<MainEffect>
    fun dispatch(intent: MainIntent)
}

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
