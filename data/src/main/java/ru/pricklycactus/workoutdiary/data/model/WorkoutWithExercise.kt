package ru.pricklycactus.workoutdiary.data.model

import androidx.room.Embedded
import androidx.room.Relation
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.database.Workout

data class WorkoutWithExercise(
    @Embedded val workout: Workout,
    @Relation(
        parentColumn = "exerciseId",
        entityColumn = "id"
    )
    val exercise: Exercise
)
