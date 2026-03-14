package ru.pricklycactus.workoutdiary.feature.workout

sealed interface WorkoutUserEvent {
    data class IncreaseReps(val exerciseId: Long) : WorkoutUserEvent
    data class DecreaseReps(val exerciseId: Long) : WorkoutUserEvent
    data class IncreaseSets(val exerciseId: Long) : WorkoutUserEvent
    data class DecreaseSets(val exerciseId: Long) : WorkoutUserEvent
    data class StartNow(val exerciseId: Long) : WorkoutUserEvent
    data class CompleteExercise(val exerciseId: Long) : WorkoutUserEvent
    data object FinishClick : WorkoutUserEvent
    data object ConfirmFinish : WorkoutUserEvent
    data object DismissFinishDialog : WorkoutUserEvent
}
