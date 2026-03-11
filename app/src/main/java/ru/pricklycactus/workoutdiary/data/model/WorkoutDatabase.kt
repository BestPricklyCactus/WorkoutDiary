package ru.pricklycactus.workoutdiary.data.model

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
//import androidx.room.TypeConverters
import android.content.Context
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.database.Workout

@Database(
    entities = [Exercise::class, Workout::class],
    version = 1,
    exportSchema = false
)
//@TypeConverters(ConvertersWD::class)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: WorkoutDatabase? = null

        fun getDatabase(context: Context): WorkoutDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkoutDatabase::class.java,
                    "workout_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
