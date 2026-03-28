package ru.pricklycactus.workoutdiary.feature.workout.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.pricklycactus.workoutdiary.core.mvi.MviEffect
import ru.pricklycactus.workoutdiary.core.mvi.MviIntent

interface WorkoutStore {
    val state: StateFlow<WorkoutViewState>
    val effect: Flow<WorkoutEffect>
    fun dispatch(intent: WorkoutIntent)
}

sealed interface WorkoutIntent : MviIntent {
    data class IncreaseReps(val exerciseId: Long) : WorkoutIntent
    data class DecreaseReps(val exerciseId: Long) : WorkoutIntent
    data class IncreaseSets(val exerciseId: Long) : WorkoutIntent
    data class DecreaseSets(val exerciseId: Long) : WorkoutIntent
    data class StartNow(val exerciseId: Long) : WorkoutIntent
    data class CompleteExercise(val exerciseId: Long) : WorkoutIntent
    data object FinishClick : WorkoutIntent
    data object ConfirmFinish : WorkoutIntent
    data object DismissFinishDialog : WorkoutIntent
}

sealed interface WorkoutEffect : MviEffect {
    data object NavigateBack : WorkoutEffect
}
