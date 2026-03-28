package ru.pricklycactus.workoutdiary.feature.editor.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.core.mvi.MviStore
import ru.pricklycactus.workoutdiary.data.domain.ExerciseDomain
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
        loadExercises()
    }

    override fun dispatch(intent: EditorIntent) {
        when (intent) {
            EditorIntent.AddExerciseClick -> updateState {
                it.copy(
                    showAddExerciseForm = true,
                    editingExerciseId = null,
                    exerciseName = "",
                    exerciseDescription = ""
                )
            }
            is EditorIntent.EditExerciseClick -> {
                val exercise = currentState.exercises.find { it.id == intent.exerciseId }
                if (exercise != null) {
                    updateState {
                        it.copy(
                            showAddExerciseForm = true,
                            editingExerciseId = exercise.id,
                            exerciseName = exercise.name,
                            exerciseDescription = exercise.description
                        )
                    }
                }
            }
            EditorIntent.CancelAddExerciseClick -> updateState {
                it.copy(showAddExerciseForm = false, editingExerciseId = null)
            }
            EditorIntent.SaveExerciseClick -> saveExercise()
            is EditorIntent.OnTextChanged -> handleTextChanged(intent.field, intent.text)
            is EditorIntent.OnExerciseSelected -> handleExerciseSelected(intent.exerciseId, intent.selected)
            is EditorIntent.OnExercisesDelete -> deleteExercises(intent.exerciseIds)
            EditorIntent.LoadExercises -> loadExercises()
        }
    }

    private fun saveExercise() {
        if (currentState.exerciseName.isBlank()) return

        scope.launch {
            val exercise = ExerciseDomain(
                id = currentState.editingExerciseId ?: 0,
                name = currentState.exerciseName,
                description = currentState.exerciseDescription
            )
            repository.upsertExercise(exercise)
            updateState { it.copy(showAddExerciseForm = false, editingExerciseId = null) }
        }
    }

    private fun handleTextChanged(field: String, text: String) {
        updateState {
            when (field) {
                EditorFieldKeys.Name -> it.copy(exerciseName = text)
                EditorFieldKeys.Description -> it.copy(exerciseDescription = text)
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

internal object EditorFieldKeys {
    const val Name = "name"
    const val Description = "description"
}
