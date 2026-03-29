package ru.pricklycactus.workoutdiary.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.pricklycactus.workoutdiary.data.dao.ExerciseDao
import ru.pricklycactus.workoutdiary.data.dao.WorkoutDao
import ru.pricklycactus.workoutdiary.data.database.WorkoutDatabase
import ru.pricklycactus.workoutdiary.feature.aiworkout.impl.BuildConfig as AiWorkoutBuildConfig
import ru.pricklycactus.workoutdiary.feature.aiworkout.impl.LlmWorkoutGenerator
import javax.inject.Singleton

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

    @Provides
    @Singleton
    fun provideLlmWorkoutGenerator(): LlmWorkoutGenerator {
        return LlmWorkoutGenerator(
            apiKey = AiWorkoutBuildConfig.LLM_API_KEY,
            model = AiWorkoutBuildConfig.LLM_MODEL
        )
    }
}
