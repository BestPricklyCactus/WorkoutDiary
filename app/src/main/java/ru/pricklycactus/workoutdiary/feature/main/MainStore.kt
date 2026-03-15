package ru.pricklycactus.workoutdiary.feature.main

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.core.mvi.MviStore
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository

class MainStore(
    private val repository: WorkoutRepository,
    scope: CoroutineScope
) : MviStore<MainViewState, MainIntent, MainEffect>(MainViewState(), scope) {

    init {
        observeExercises()
    }

    private fun observeExercises() {
        scope.launch {
            repository.getAllExercises().collectLatest { exercises ->
                updateState { it.copy(exercises = exercises) }
            }
        }
    }

    override fun dispatch(intent: MainIntent) {
        when (intent) {
            is MainIntent.OnClick -> handleOnClick(intent.action)
            is MainIntent.OnTextChanged -> handleTextChanged(intent.field, intent.text)
            is MainIntent.OnExerciseSelected -> handleExerciseSelected(intent.exerciseId, intent.selected)
            is MainIntent.OnExercisesDelete -> handleExercisesDelete(intent.exerciseIds)
            MainIntent.NavigateToWorkout -> sendEffect(MainEffect.NavigateToWorkout)
        }
    }

    private fun handleOnClick(action: String) {
        when (action) {
            "add_exercise" -> updateState {
                it.copy(showAddExerciseForm = true, exerciseName = "", exerciseDescription = "")
            }
            "show_exercises" -> updateState {
                it.copy(showExercisesList = true, showAddExerciseForm = false)
            }
            "save_exercise" -> {
                scope.launch {
                    val exercise = Exercise(
                        name = currentState.exerciseName,
                        description = currentState.exerciseDescription
                    )
                    repository.insertExercise(exercise)
                    updateState {
                        it.copy(showAddExerciseForm = false, exerciseName = "", exerciseDescription = "")
                    }
                }
            }
            "cancel_add_exercise" -> updateState {
                it.copy(showAddExerciseForm = false, exerciseName = "", exerciseDescription = "")
            }
        }
    }

    private fun handleTextChanged(field: String, text: String) {
        updateState {
            when (field) {
                "name" -> it.copy(exerciseName = text)
                "description" -> it.copy(exerciseDescription = text)
                else -> it
            }
        }
    }

    private fun handleExerciseSelected(exerciseId: Long, selected: Boolean) {
        val currentSelected = currentState.selectedExerciseIds.toMutableSet()
        if (selected) currentSelected.add(exerciseId) else currentSelected.remove(exerciseId)
        updateState { it.copy(selectedExerciseIds = currentSelected) }
    }

    private fun handleExercisesDelete(exerciseIds: Set<Long>) {
        scope.launch {
            val exercisesToDelete = currentState.exercises.filter { it.id in exerciseIds }
            exercisesToDelete.forEach { repository.deleteExercise(it) }
            updateState { it.copy(selectedExerciseIds = it.selectedExerciseIds - exerciseIds) }
        }
    }
}
