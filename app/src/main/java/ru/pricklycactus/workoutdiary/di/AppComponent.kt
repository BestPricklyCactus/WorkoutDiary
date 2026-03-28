package ru.pricklycactus.workoutdiary.di

import dagger.Component
import ru.pricklycactus.workoutdiary.MainActivity
import ru.pricklycactus.workoutdiary.WorkoutDiaryApplication
import javax.inject.Singleton

@Component(modules = [AppModule::class])
@Singleton
interface AppComponent {
    fun inject(app: WorkoutDiaryApplication)
    fun inject(activity: MainActivity)
}
