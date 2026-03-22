package ru.pricklycactus.workoutdiary.data.domain

import ru.pricklycactus.workoutdiary.data.database.Exercise as ExerciseEntity
import ru.pricklycactus.workoutdiary.data.database.Workout as WorkoutEntity
import ru.pricklycactus.workoutdiary.data.model.WorkoutWithExercise as WorkoutWithExerciseEntity

fun ExerciseEntity.toDomain() = ExerciseDomain(
    id = id,
    name = name,
    description = description
)

fun ExerciseDomain.toEntity() = ExerciseEntity(
    id = id,
    name = name,
    description = description
)

fun WorkoutEntity.toDomain() = WorkoutDomain(
    id = id,
    exerciseId = exerciseId,
    reps = reps,
    sets = sets,
    date = workoutDate,
    totalDurationMillis = totalDurationMillis,
    exerciseDurationMillis = exerciseDurationMillis
)

fun WorkoutDomain.toEntity() = WorkoutEntity(
    id = id,
    exerciseId = exerciseId,
    reps = reps,
    sets = sets,
    workoutDate = date,
    totalDurationMillis = totalDurationMillis,
    exerciseDurationMillis = exerciseDurationMillis
)

fun WorkoutWithExerciseEntity.toDomain() = WorkoutWithExerciseDomain(
    workout = workout.toDomain(),
    exercise = exercise.toDomain()
)
