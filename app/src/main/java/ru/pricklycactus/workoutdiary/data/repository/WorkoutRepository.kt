package ru.pricklycactus.workoutdiary.data.repository

import kotlinx.coroutines.flow.Flow
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.database.Workout
import ru.pricklycactus.workoutdiary.data.dao.ExerciseDao
import ru.pricklycactus.workoutdiary.data.dao.WorkoutDao
import ru.pricklycactus.workoutdiary.data.model.WorkoutWithExercise

class WorkoutRepository(
    private val exerciseDao: ExerciseDao,
    private val workoutDao: WorkoutDao
) {
    fun getAllExercises(): Flow<List<Exercise>> = exerciseDao.getAllExercises()
    fun getAllWorkouts(): Flow<List<Workout>> = workoutDao.getAllWorkouts()
    fun getWorkoutsWithExercises(): Flow<List<WorkoutWithExercise>> = workoutDao.getWorkoutsWithExercises()

    suspend fun insertExercise(exercise: Exercise) = exerciseDao.insertExercise(exercise)
    suspend fun insertWorkout(workout: Workout) = workoutDao.insertWorkout(workout)
    suspend fun insertWorkouts(workouts: List<Workout>) = workoutDao.insertWorkouts(workouts)
}
