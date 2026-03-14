package ru.pricklycactus.workoutdiary.di

import dagger.Component
import javax.inject.Singleton
import ru.pricklycactus.workoutdiary.MainActivity
import ru.pricklycactus.workoutdiary.WorkoutDiaryApplication

@Component(modules = [AppModule::class])
@Singleton
interface AppComponent {
    fun inject(app: WorkoutDiaryApplication)
    fun inject(activity: MainActivity)
}
