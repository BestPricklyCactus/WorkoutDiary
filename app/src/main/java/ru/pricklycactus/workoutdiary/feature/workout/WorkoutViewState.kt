package ru.pricklycactus.workoutdiary.feature.workout

import ru.pricklycactus.workoutdiary.core.mvi.MviState
import ru.pricklycactus.workoutdiary.data.database.Exercise

data class WorkoutExerciseState(
    val exercise: Exercise,
    val reps: Int = 8,
    val sets: Int = 3,
    val status: WorkoutExerciseStatus = WorkoutExerciseStatus.IDLE,
    val startedAtMillis: Long? = null,
    val completedAtMillis: Long? = null,
    val durationMillis: Long? = null
)

enum class WorkoutExerciseStatus {
    IDLE,
    IN_PROGRESS,
    COMPLETED
}

data class WorkoutViewState(
    val exercises: List<WorkoutExerciseState> = emptyList(),
    val workoutStartedAtMillis: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val showFinishConfirmation: Boolean = false
) : MviState
