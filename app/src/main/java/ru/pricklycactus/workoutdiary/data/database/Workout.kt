package ru.pricklycactus.workoutdiary.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "exerciseId")
    val exerciseId: Long,
    @ColumnInfo(name = "reps")
    val reps: Int,
    @ColumnInfo(name = "sets")
    val sets: Int
)