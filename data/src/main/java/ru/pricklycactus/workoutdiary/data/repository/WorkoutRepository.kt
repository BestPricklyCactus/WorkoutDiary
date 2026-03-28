package ru.pricklycactus.workoutdiary.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.pricklycactus.workoutdiary.data.dao.ExerciseDao
import ru.pricklycactus.workoutdiary.data.dao.WorkoutDao
import ru.pricklycactus.workoutdiary.data.domain.ExerciseDomain
import ru.pricklycactus.workoutdiary.data.domain.WorkoutDomain
import ru.pricklycactus.workoutdiary.data.domain.WorkoutWithExerciseDomain
import ru.pricklycactus.workoutdiary.data.domain.toDomain
import ru.pricklycactus.workoutdiary.data.domain.toEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao
) {
    fun getAllExercises(): Flow<List<ExerciseDomain>> = 
        exerciseDao.getAllExercises().map { entities -> entities.map { it.toDomain() } }

    fun getAllWorkouts(): Flow<List<WorkoutDomain>> = 
        workoutDao.getAllWorkouts().map { entities -> entities.map { it.toDomain() } }

    fun getWorkoutsWithExercises(): Flow<List<WorkoutWithExerciseDomain>> = 
        workoutDao.getWorkoutsWithExercises().map { entities -> entities.map { it.toDomain() } }

    suspend fun getLastWorkoutForExercise(exerciseId: Long): WorkoutDomain? = 
        workoutDao.getLastWorkoutForExercise(exerciseId)?.toDomain()

    suspend fun upsertExercise(exercise: ExerciseDomain) = 
        exerciseDao.upsertExercise(exercise.toEntity())

    suspend fun deleteExercise(exercise: ExerciseDomain) = 
        exerciseDao.deleteExercise(exercise.toEntity())

    suspend fun insertWorkout(workout: WorkoutDomain) = 
        workoutDao.insertWorkout(workout.toEntity())

    suspend fun insertWorkouts(workouts: List<WorkoutDomain>) = 
        workoutDao.insertWorkouts(workouts.map { it.toEntity() })
}
