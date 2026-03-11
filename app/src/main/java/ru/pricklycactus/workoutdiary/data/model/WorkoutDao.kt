package ru.pricklycactus.workoutdiary.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.pricklycactus.workoutdiary.data.database.Workout

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Query("SELECT w.*, e.name as exerciseName FROM workouts w JOIN exercises e ON w.exerciseId = e.id")
    fun getWorkoutsWithExercises(): Flow<List<WorkoutWithExercise>>

    @Insert
    suspend fun insertWorkout(workout: Workout)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)
}
