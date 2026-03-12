package ru.pricklycactus.workoutdiary.ui

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.model.WorkoutDatabase
import ru.pricklycactus.workoutdiary.data.model.WorkoutDatabaseProvider

class MainViewModel (
    private val context: Context
): ViewModel() {

    private companion object {
        const val TAG = "MainViewModel"
    }

    private val _viewState = MutableStateFlow(MainViewState())
    val viewState: StateFlow<MainViewState> = _viewState

    private fun updateState(reducer: (MainViewState) -> MainViewState) {
        val previousState = _viewState.value
        val newState = reducer(previousState)
        logStateChange(previousState, newState)
        _viewState.value = newState
    }

    fun processEvent(event: MainUserEvent) {
        logEvent(event)
        when (event) {
            is MainUserEvent.OnClick -> {
                // Обработка кликов
                when (event.action) {
                    "add_exercise" -> {
                        updateState {
                            it.copy(
                                showAddExerciseForm = true,
                                exerciseName = "",
                                exerciseDescription = ""
                            )
                        }
                    }
                    "show_exercises" -> {
                        // Логика открытия списка упражнений
                        loadExercises()
                        updateState {
                            it.copy(
                                showExercisesList = true,
                                showAddExerciseForm = false
                            )
                        }
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
                        updateState {
                            it.copy(
                                showAddExerciseForm = false,
                                exerciseName = "",
                                exerciseDescription = ""
                            )
                        }
                    }
                    "cancel_add_exercise" -> {
                        updateState {
                            it.copy(
                                showAddExerciseForm = false,
                                exerciseName = "",
                                exerciseDescription = ""
                            )
                        }
                    }
                    "start_workout" -> {

                        // Логика начала тренировки
                        // Здесь можно перейти к экрану тренировки
                    }
                    "back_to_main" -> {
                        // Возвращаемся к главному экрану
                        updateState {
                            it.copy(
                                showExercisesList = false,
                                showAddExerciseForm = false,
                                selectedExerciseIds = emptySet()
                            )
                        }
                    }
                }
            }
            is MainUserEvent.OnTextChanged -> {
                when (event.field) {
                    "name" -> {
                        updateState { it.copy(exerciseName = event.text) }
                    }
                    "description" -> {
                        updateState { it.copy(exerciseDescription = event.text) }
                    }
                }
            }
            is MainUserEvent.OnExerciseSelected -> {
                val currentSelected = _viewState.value.selectedExerciseIds.toMutableSet()
                if (event.selected) {
                    currentSelected.add(event.exerciseId)
                } else {
                    currentSelected.remove(event.exerciseId)
                }
                updateState { it.copy(selectedExerciseIds = currentSelected) }
            }
            is MainUserEvent.OnExercisesLoaded -> {

            }
        }
    }

    private fun loadExercises() {
        viewModelScope.launch {
            val exercises = WorkoutDatabase.getDatabase(context).exerciseDao().getAllExercises().first()
            updateState { it.copy(exercises = exercises) }
        }
    }

    private fun logEvent(event: MainUserEvent) {
        if (!isDebugLoggingEnabled()) return

        val message = when (event) {
            is MainUserEvent.OnClick -> "Event: click action=${event.action}"
            is MainUserEvent.OnTextChanged -> {
                "Event: text_changed field=${event.field}, value=${event.text}"
            }
            is MainUserEvent.OnExerciseSelected -> {
                "Event: exercise_selected id=${event.exerciseId}, selected=${event.selected}"
            }
            is MainUserEvent.OnExercisesLoaded -> {
                "Event: exercises_loaded count=${event.exercises.size}"
            }
        }

        Log.d(TAG, message)
    }

    private fun logStateChange(previous: MainViewState, current: MainViewState) {
        if (!isDebugLoggingEnabled() || previous == current) return

        val changes = buildList {
            if (previous.searchText != current.searchText) {
                add("searchText=${current.searchText}")
            }
            if (previous.showAddExerciseForm != current.showAddExerciseForm) {
                add("showAddExerciseForm=${current.showAddExerciseForm}")
            }
            if (previous.exerciseName != current.exerciseName) {
                add("exerciseName=${current.exerciseName}")
            }
            if (previous.exerciseDescription != current.exerciseDescription) {
                add("exerciseDescription=${current.exerciseDescription}")
            }
            if (previous.showExercisesList != current.showExercisesList) {
                add("showExercisesList=${current.showExercisesList}")
            }
            if (previous.exercises != current.exercises) {
                add("exercisesCount=${current.exercises.size}")
            }
            if (previous.selectedExerciseIds != current.selectedExerciseIds) {
                add("selectedExerciseIds=${current.selectedExerciseIds}")
            }
        }

        Log.d(TAG, "State changed: ${changes.joinToString()}")
    }

    private fun isDebugLoggingEnabled(): Boolean {
        return context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }
}
