package ru.pricklycactus.workoutdiary.feature.workout.api

import ru.pricklycactus.workoutdiary.core.mvi.MviState
import ru.pricklycactus.workoutdiary.data.domain.ExerciseDomain

data class WorkoutViewState(
    val exercises: List<WorkoutExerciseState> = emptyList(),
    val isSaving: Boolean = false,
    val showFinishConfirmation: Boolean = false,
    val workoutStartedAtMillis: Long = System.currentTimeMillis()
) : MviState

data class WorkoutExerciseState(
    val exercise: ExerciseDomain,
    val status: WorkoutExerciseStatus = WorkoutExerciseStatus.IDLE,
    val sets: Int = 0,
    val reps: Int = 0,
    val startedAtMillis: Long? = null,
    val completedAtMillis: Long? = null,
    val durationMillis: Long? = null
)

enum class WorkoutExerciseStatus {
    IDLE, IN_PROGRESS, COMPLETED
}
