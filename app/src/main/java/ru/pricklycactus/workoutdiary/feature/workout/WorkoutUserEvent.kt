package ru.pricklycactus.workoutdiary.feature.workout

sealed interface WorkoutUserEvent {
    data class ExerciseClicked(val exerciseId: Long) : WorkoutUserEvent
    data class IncreaseReps(val exerciseId: Long) : WorkoutUserEvent
    data class DecreaseReps(val exerciseId: Long) : WorkoutUserEvent
    data class IncreaseSets(val exerciseId: Long) : WorkoutUserEvent
    data class DecreaseSets(val exerciseId: Long) : WorkoutUserEvent
    data class StartNow(val exerciseId: Long) : WorkoutUserEvent
    data class CompleteExercise(val exerciseId: Long) : WorkoutUserEvent
    data object DismissDialog : WorkoutUserEvent
}
