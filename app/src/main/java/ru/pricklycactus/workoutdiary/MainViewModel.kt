package ru.pricklycactus.workoutdiary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.content.Context
import ru.pricklycactus.workoutdiary.data.model.WorkoutDatabase
import ru.pricklycactus.workoutdiary.data.model.WorkoutDatabaseProvider
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository
import ru.pricklycactus.workoutdiary.intent.UserEvent

class MainViewModel(private val databaseProvider: WorkoutDatabaseProvider) : ViewModel() {
    private val repository: WorkoutRepository

    init {
        val database = databaseProvider.getDatabase()
        repository = WorkoutRepository(database.exerciseDao(), database.workoutDao())
    }

    private val _viewState = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> = _viewState

    fun processIntent(intent: UserEvent) {
        when (intent) {
            is UserEvent.OnClick -> {
                // Обработка клика
            }
            is UserEvent.OnTextChanged -> {
                _viewState.value = _viewState.value?.copy(searchText = intent.text)
            }
        }
    }
}
