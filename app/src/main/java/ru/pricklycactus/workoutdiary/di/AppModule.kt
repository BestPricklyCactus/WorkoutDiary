package ru.pricklycactus.workoutdiary.di

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import android.content.Context
import ru.pricklycactus.workoutdiary.data.database.WorkoutDatabase
import ru.pricklycactus.workoutdiary.data.dao.ExerciseDao
import ru.pricklycactus.workoutdiary.data.dao.WorkoutDao

@Module
class AppModule(private val context: Context) {
    @Provides
    @Singleton
    fun provideContext(): Context = context

    @Provides
    @Singleton
    fun provideDatabase(context: Context): WorkoutDatabase {
        return WorkoutDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun provideExerciseDao(database: WorkoutDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    @Provides
    @Singleton
    fun provideWorkoutDao(database: WorkoutDatabase): WorkoutDao {
        return database.workoutDao()
    }
}
