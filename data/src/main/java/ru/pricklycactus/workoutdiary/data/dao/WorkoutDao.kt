package ru.pricklycactus.workoutdiary.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.pricklycactus.workoutdiary.data.database.Workout
import ru.pricklycactus.workoutdiary.data.model.WorkoutWithExercise

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Transaction
    @Query("SELECT * FROM workouts")
    fun getWorkoutsWithExercises(): Flow<List<WorkoutWithExercise>>

    @Query("SELECT * FROM workouts WHERE exerciseId = :exerciseId ORDER BY workoutDate DESC LIMIT 1")
    suspend fun getLastWorkoutForExercise(exerciseId: Long): Workout?

    @Insert
    suspend fun insertWorkout(workout: Workout)

    @Insert
    suspend fun insertWorkouts(workouts: List<Workout>)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Query("DELETE FROM workouts WHERE workoutDate = :workoutDate")
    suspend fun deleteWorkoutsByDate(workoutDate: Long)
}
