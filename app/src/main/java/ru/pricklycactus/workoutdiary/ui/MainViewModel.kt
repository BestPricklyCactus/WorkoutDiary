package ru.pricklycactus.workoutdiary.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.model.WorkoutDatabaseProvider

class MainViewModel (
    private val context: Context
): ViewModel() {

    private val _viewState = MutableStateFlow(MainViewState())
    val viewState: StateFlow<MainViewState> = _viewState

    fun processEvent(event: MainUserEvent) {
        when (event) {
            is MainUserEvent.OnClick -> {
                // Обработка кликов
                when (event.action) {
                    "add_exercise" -> {
                        _viewState.value = _viewState.value.copy(
                            showAddExerciseForm = true,
                            exerciseName = "",
                            exerciseDescription = ""
                        )
                    }
                    "show_exercises" -> {
                        // Логика открытия списка упражнений
                    }
                    "save_exercise" -> {
                        // Логика сохранения упражнения
                        // Сохраняем упражнение в базу данных
                        viewModelScope.launch {
                            val exercise = Exercise(
                                name = _viewState.value.exerciseName,
                                description = _viewState.value.exerciseDescription
                            )
                            WorkoutDatabaseProvider.getDatabase(context).exerciseDao().insertExercise(exercise)
                        }
                        // Сбрасываем форму
                        _viewState.value = _viewState.value.copy(
                            showAddExerciseForm = false,
                            exerciseName = "",
                            exerciseDescription = ""
                        )
                    }
                    "cancel_add_exercise" -> {
                        _viewState.value = _viewState.value.copy(
                            showAddExerciseForm = false,
                            exerciseName = "",
                            exerciseDescription = ""
                        )
                    }
                }
            }
            is MainUserEvent.OnTextChanged -> {
                when (event.field) {
                    "name" -> {
                        _viewState.value = _viewState.value.copy(exerciseName = event.text)
                    }
                    "description" -> {
                        _viewState.value = _viewState.value.copy(exerciseDescription = event.text)
                    }
                }
            }
        }
    }
}
