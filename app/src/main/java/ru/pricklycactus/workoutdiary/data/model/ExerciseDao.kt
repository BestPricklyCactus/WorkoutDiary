package ru.pricklycactus.workoutdiary.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import kotlinx.coroutines.flow.Flow
import ru.pricklycactus.workoutdiary.data.database.Exercise

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    fun getAllExercises(): Flow<List<Exercise>>

    @Insert
    suspend fun insertExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)
}