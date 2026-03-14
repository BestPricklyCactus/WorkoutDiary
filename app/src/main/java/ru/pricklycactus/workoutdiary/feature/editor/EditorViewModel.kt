package ru.pricklycactus.workoutdiary.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository

class EditorViewModel(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow(EditorViewState())
    val viewState: StateFlow<EditorViewState> = _viewState.asStateFlow()

    init {
        processEvent(EditorUserEvent.LoadExercises)
    }

    fun processEvent(event: EditorUserEvent) {
        when (event) {
            EditorUserEvent.AddExerciseClick -> {
                _viewState.update { it.copy(showAddExerciseForm = true, exerciseName = "", exerciseDescription = "") }
            }
            EditorUserEvent.CancelAddExerciseClick -> {
                _viewState.update { it.copy(showAddExerciseForm = false) }
            }
            EditorUserEvent.SaveExerciseClick -> {
                viewModelScope.launch {
                    val exercise = Exercise(
                        name = _viewState.value.exerciseName,
                        description = _viewState.value.exerciseDescription
                    )
                    repository.insertExercise(exercise)
                    _viewState.update { it.copy(showAddExerciseForm = false) }
                    loadExercises()
                }
            }
            is EditorUserEvent.OnTextChanged -> {
                when (event.field) {
                    "name" -> _viewState.update { it.copy(exerciseName = event.text) }
                    "description" -> _viewState.update { it.copy(exerciseDescription = event.text) }
                }
            }
            is EditorUserEvent.OnExerciseSelected -> {
                val currentSelected = _viewState.value.selectedExerciseIds.toMutableSet()
                if (event.selected) currentSelected.add(event.exerciseId)
                else currentSelected.remove(event.exerciseId)
                _viewState.update { it.copy(selectedExerciseIds = currentSelected) }
            }
            is EditorUserEvent.OnExercisesDelete -> {
                viewModelScope.launch {
                    val exercisesToDelete = _viewState.value.exercises.filter { it.id in event.exerciseIds }
                    exercisesToDelete.forEach { repository.deleteExercise(it) }
                    loadExercises()
                    _viewState.update { it.copy(selectedExerciseIds = emptySet()) }
                }
            }
            EditorUserEvent.LoadExercises -> loadExercises()
        }
    }

    private fun loadExercises() {
        viewModelScope.launch {
            val exercises = repository.getAllExercises().first()
            _viewState.update { it.copy(exercises = exercises) }
        }
    }
}
