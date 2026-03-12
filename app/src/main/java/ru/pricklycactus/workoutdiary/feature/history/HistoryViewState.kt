package ru.pricklycactus.workoutdiary.feature.history

data class WorkoutHistoryItem(
    val exerciseName: String,
    val sets: Int,
    val reps: Int
)

data class WorkoutHistoryEntry(
    val workoutDate: Long,
    val totalDurationMillis: Long,
    val exercises: List<WorkoutHistoryItem>
)

data class HistoryViewState(
    val workouts: List<WorkoutHistoryEntry> = emptyList()
)
