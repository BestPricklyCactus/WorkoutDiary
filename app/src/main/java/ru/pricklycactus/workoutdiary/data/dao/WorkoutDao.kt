package ru.pricklycactus.workoutdiary.data.dao

import ru.pricklycactus.workoutdiary.data.model.WorkoutWithExercise

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.pricklycactus.workoutdiary.data.database.Workout

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Transaction
    @Query("SELECT * FROM workouts")
    fun getWorkoutsWithExercises(): Flow<List<WorkoutWithExercise>>

    @Insert
    suspend fun insertWorkout(workout: Workout)

    @Insert
    suspend fun insertWorkouts(workouts: List<Workout>)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)
}
