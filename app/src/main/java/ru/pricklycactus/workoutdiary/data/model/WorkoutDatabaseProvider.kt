package ru.pricklycactus.workoutdiary.data.model;

interface WorkoutDatabaseProvider {
    fun getDatabase(): WorkoutDatabase
}
