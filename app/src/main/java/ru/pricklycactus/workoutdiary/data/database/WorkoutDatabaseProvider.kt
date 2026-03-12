package ru.pricklycactus.workoutdiary.data.database

import android.content.Context

object WorkoutDatabaseProvider {
    private var database: WorkoutDatabase? = null

    fun getDatabase(context: Context): WorkoutDatabase {
        if (database == null) {
            database = WorkoutDatabase.getDatabase(context)
        }
        return database!!
    }
}
