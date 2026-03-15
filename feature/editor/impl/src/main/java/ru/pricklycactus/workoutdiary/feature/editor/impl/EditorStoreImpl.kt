package ru.pricklycactus.workoutdiary.feature.editor.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.core.mvi.MviStore
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository
import ru.pricklycactus.workoutdiary.feature.editor.api.EditorEffect
import ru.pricklycactus.workoutdiary.feature.editor.api.EditorIntent
import ru.pricklycactus.workoutdiary.feature.editor.api.EditorStore
import ru.pricklycactus.workoutdiary.feature.editor.api.EditorViewState

class EditorStoreImpl(
    private val repository: WorkoutRepository,
    scope: CoroutineScope
) : MviStore<EditorViewState, EditorIntent, EditorEffect>(EditorViewState(), scope), EditorStore {

    init {
        dispatch(EditorIntent.LoadExercises)
    }

    override fun dispatch(intent: EditorIntent) {
        when (intent) {
            EditorIntent.AddExerciseClick -> updateState {
                it.copy(showAddExerciseForm = true, exerciseName = "", exerciseDescription = "")
            }
            EditorIntent.CancelAddExerciseClick -> updateState {
                it.copy(showAddExerciseForm = false)
            }
            EditorIntent.SaveExerciseClick -> saveExercise()
            is EditorIntent.OnTextChanged -> handleTextChanged(intent.field, intent.text)
            is EditorIntent.OnExerciseSelected -> handleExerciseSelected(intent.exerciseId, intent.selected)
            is EditorIntent.OnExercisesDelete -> deleteExercises(intent.exerciseIds)
            EditorIntent.LoadExercises -> loadExercises()
        }
    }

    private fun saveExercise() {
        scope.launch {
            val exercise = Exercise(
                name = currentState.exerciseName,
                description = currentState.exerciseDescription
            )
            repository.insertExercise(exercise)
            updateState { it.copy(showAddExerciseForm = false) }
            loadExercises()
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

    private fun deleteExercises(exerciseIds: Set<Long>) {
        scope.launch {
            val exercisesToDelete = currentState.exercises.filter { it.id in exerciseIds }
            exercisesToDelete.forEach { repository.deleteExercise(it) }
            loadExercises()
            updateState { it.copy(selectedExerciseIds = emptySet()) }
        }
    }

    private fun loadExercises() {
        scope.launch {
            repository.getAllExercises().collectLatest { exercises ->
                updateState { it.copy(exercises = exercises) }
            }
        }
    }
}
