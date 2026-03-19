package ru.pricklycactus.workoutdiary.feature.main.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.core.mvi.MviStore
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository
import ru.pricklycactus.workoutdiary.feature.main.api.MainEffect
import ru.pricklycactus.workoutdiary.feature.main.api.MainIntent
import ru.pricklycactus.workoutdiary.feature.main.api.MainStore
import ru.pricklycactus.workoutdiary.feature.main.api.MainViewState

class MainStoreImpl(
    private val repository: WorkoutRepository,
    scope: CoroutineScope
) : MviStore<MainViewState, MainIntent, MainEffect>(MainViewState(), scope), MainStore {

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
            MainStoreKeys.AddExercise -> updateState {
                it.copy(showAddExerciseForm = true, exerciseName = "", exerciseDescription = "")
            }
            MainActions.ShowExercises -> updateState {
                it.copy(showExercisesList = true, showAddExerciseForm = false)
            }
        }
    }

    private fun handleTextChanged(field: String, text: String) {
        updateState {
            when (field) {
                MainStoreKeys.NameField -> it.copy(exerciseName = text)
                MainStoreKeys.DescriptionField -> it.copy(exerciseDescription = text)
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

internal object MainStoreKeys {
    const val AddExercise = "add_exercise"
    const val NameField = "name"
    const val DescriptionField = "description"
}
