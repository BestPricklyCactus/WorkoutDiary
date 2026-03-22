package ru.pricklycactus.workoutdiary.data.domain

data class ExerciseDomain(
    val id: Long = 0,
    val name: String,
    val description: String
)

data class WorkoutDomain(
    val id: Long = 0,
    val exerciseId: Long,
    val reps: Int,
    val sets: Int,
    val date: Long,
    val totalDurationMillis: Long,
    val exerciseDurationMillis: Long
)

data class WorkoutWithExerciseDomain(
    val workout: WorkoutDomain,
    val exercise: ExerciseDomain
)
