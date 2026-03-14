package ru.pricklycactus.workoutdiary

import android.app.Application
import ru.pricklycactus.workoutdiary.di.AppComponent
import ru.pricklycactus.workoutdiary.di.AppModule
import ru.pricklycactus.workoutdiary.di.DaggerAppComponent

class WorkoutDiaryApplication : Application() {
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
        appComponent.inject(this)
    }
}
