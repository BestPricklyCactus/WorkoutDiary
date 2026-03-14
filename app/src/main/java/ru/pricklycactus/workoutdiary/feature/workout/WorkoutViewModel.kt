package ru.pricklycactus.workoutdiary.feature.workout

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.data.database.Workout
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository

class WorkoutViewModel(
    selectedExercises: List<Exercise>,
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow(
        WorkoutViewState(
            exercises = selectedExercises.map { exercise ->
                WorkoutExerciseState(exercise = exercise)
            }
        )
    )
    val viewState: StateFlow<WorkoutViewState> = _viewState

    private var onWorkoutFinished: (() -> Unit)? = null

    fun processEvent(event: WorkoutUserEvent) {
        when (event) {
            is WorkoutUserEvent.IncreaseReps -> {
                updateExercise(event.exerciseId) { it.copy(reps = it.reps + 1) }
            }
            is WorkoutUserEvent.DecreaseReps -> {
                updateExercise(event.exerciseId) { it.copy(reps = (it.reps - 1).coerceAtLeast(0)) }
            }
            is WorkoutUserEvent.IncreaseSets -> {
                updateExercise(event.exerciseId) { it.copy(sets = it.sets + 1) }
            }
            is WorkoutUserEvent.DecreaseSets -> {
                updateExercise(event.exerciseId) { it.copy(sets = (it.sets - 1).coerceAtLeast(0)) }
            }
            is WorkoutUserEvent.StartNow -> startExercise(event.exerciseId)
            is WorkoutUserEvent.CompleteExercise -> completeExercise(event.exerciseId)
            WorkoutUserEvent.FinishClick -> {
                val hasUnfinished = _viewState.value.exercises.any { it.status != WorkoutExerciseStatus.COMPLETED }
                if (hasUnfinished) {
                    _viewState.value = _viewState.value.copy(showFinishConfirmation = true)
                } else {
                    performFinish()
                }
            }
            WorkoutUserEvent.ConfirmFinish -> {
                _viewState.value = _viewState.value.copy(showFinishConfirmation = false)
                performFinish()
            }
            WorkoutUserEvent.DismissFinishDialog -> {
                _viewState.value = _viewState.value.copy(showFinishConfirmation = false)
            }
        }
    }

    fun setOnFinishedCallback(callback: () -> Unit) {
        onWorkoutFinished = callback
    }

    private fun startExercise(exerciseId: Long) {
        val now = System.currentTimeMillis()
        updateExercise(exerciseId) {
            it.copy(
                status = WorkoutExerciseStatus.IN_PROGRESS,
                startedAtMillis = it.startedAtMillis ?: now,
                completedAtMillis = null,
                durationMillis = null
            )
        }
    }

    private fun completeExercise(exerciseId: Long) {
        val now = System.currentTimeMillis()
        updateExercise(exerciseId) {
            val startedAt = it.startedAtMillis ?: now
            it.copy(
                status = WorkoutExerciseStatus.COMPLETED,
                startedAtMillis = startedAt,
                completedAtMillis = now,
                durationMillis = now - startedAt
            )
        }
    }

    private fun performFinish() {
        if (_viewState.value.isSaving) return

        val finishedAt = System.currentTimeMillis()
        val state = _viewState.value
        
        // Save only completed exercises
        val workouts = state.exercises
            .filter { it.status == WorkoutExerciseStatus.COMPLETED }
            .map { exerciseState ->
                Workout(
                    exerciseId = exerciseState.exercise.id,
                    reps = exerciseState.reps,
                    sets = exerciseState.sets,
                    workoutDate = finishedAt,
                    totalDurationMillis = finishedAt - state.workoutStartedAtMillis,
                    exerciseDurationMillis = exerciseState.durationMillis ?: 0L
                )
            }

        if (workouts.isEmpty()) {
            onWorkoutFinished?.invoke()
            return
        }

        _viewState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            repository.insertWorkouts(workouts)
            _viewState.value = _viewState.value.copy(isSaving = false)
            onWorkoutFinished?.invoke()
        }
    }

    private fun updateExercise(
        exerciseId: Long,
        transform: (WorkoutExerciseState) -> WorkoutExerciseState
    ) {
        _viewState.value = _viewState.value.copy(
            exercises = _viewState.value.exercises.map { exerciseState ->
                if (exerciseState.exercise.id == exerciseId) {
                    transform(exerciseState)
                } else {
                    exerciseState
                }
            }
        )
    }
}
