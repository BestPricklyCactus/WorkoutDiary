package ru.pricklycactus.workoutdiary.feature.history.api

import ru.pricklycactus.workoutdiary.core.mvi.MviState

data class HistoryViewState(
    val workouts: List<WorkoutHistoryEntry> = emptyList(),
    val workoutDatePendingDeletion: Long? = null
) : MviState

data class WorkoutHistoryEntry(
    val workoutDate: Long,
    val totalDurationMillis: Long,
    val exercises: List<WorkoutHistoryItem>
)

data class WorkoutHistoryItem(
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val exerciseDurationMillis: Long
)
