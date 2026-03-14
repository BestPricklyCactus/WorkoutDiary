package ru.pricklycactus.workoutdiary.feature.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository

class MainViewModel(
    private val context: Context,
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow(MainViewState())
    val viewState: StateFlow<MainViewState> = _viewState

    init {
        observeExercises()
    }

    private fun updateState(reducer: (MainViewState) -> MainViewState) {
        val previousState = _viewState.value
        val newState = reducer(previousState)
        _viewState.value = newState
    }

    private fun observeExercises() {
        viewModelScope.launch {
            repository.getAllExercises().collectLatest { exercises ->
                updateState { it.copy(exercises = exercises) }
            }
        }
    }

    fun processEvent(event: MainUserEvent) {
        when (event) {
            is MainUserEvent.OnClick -> {
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
                        updateState {
                            it.copy(
                                showExercisesList = true,
                                showAddExerciseForm = false
                            )
                        }
                    }
                    "save_exercise" -> {
                        viewModelScope.launch {
                            val exercise = Exercise(
                                name = _viewState.value.exerciseName,
                                description = _viewState.value.exerciseDescription
                            )
                            repository.insertExercise(exercise)
                        }
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
                        // Перейти к экрану тренировки
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
            is MainUserEvent.OnExercisesDelete -> {
                viewModelScope.launch {
                    val exercisesToDelete = _viewState.value.exercises.filter {
                        it.id in event.exerciseIds
                    }
                    exercisesToDelete.forEach { repository.deleteExercise(it) }
                    updateState {
                        it.copy(
                            selectedExerciseIds = it.selectedExerciseIds - event.exerciseIds
                        )
                    }
                }
            }
            is MainUserEvent.OnExercisesLoaded -> {
                // Optional: handle event when exercises are loaded
            }
        }
    }
}
