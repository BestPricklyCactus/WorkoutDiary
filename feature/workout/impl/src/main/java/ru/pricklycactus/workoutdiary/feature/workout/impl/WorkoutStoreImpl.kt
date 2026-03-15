package ru.pricklycactus.workoutdiary.feature.workout.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.core.mvi.MviStore
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.database.Workout
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutEffect
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutExerciseStatus
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutIntent
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutStore
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutViewState
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutExerciseState

class WorkoutStoreImpl(
    selectedExercises: List<Exercise>,
    private val repository: WorkoutRepository,
    scope: CoroutineScope
) : MviStore<WorkoutViewState, WorkoutIntent, WorkoutEffect>(
    WorkoutViewState(
        exercises = selectedExercises.map { exercise ->
            WorkoutExerciseState(
                exercise = exercise,
                sets = 3,
                reps = 8
            )
        }
    ),
    scope
), WorkoutStore {

    init {
        loadLastValues()
    }

    private fun loadLastValues() {
        scope.launch {
            val updatedExercises = currentState.exercises.map { exerciseState ->
                val lastWorkout = repository.getLastWorkoutForExercise(exerciseState.exercise.id)
                if (lastWorkout != null) {
                    exerciseState.copy(
                        sets = lastWorkout.sets,
                        reps = lastWorkout.reps
                    )
                } else {
                    exerciseState
                }
            }
            updateState { it.copy(exercises = updatedExercises) }
        }
    }

    override fun dispatch(intent: WorkoutIntent) {
        when (intent) {
            is WorkoutIntent.IncreaseReps -> updateExercise(intent.exerciseId) { it.copy(reps = it.reps + 1) }
            is WorkoutIntent.DecreaseReps -> updateExercise(intent.exerciseId) { it.copy(reps = (it.reps - 1).coerceAtLeast(0)) }
            is WorkoutIntent.IncreaseSets -> updateExercise(intent.exerciseId) { it.copy(sets = it.sets + 1) }
            is WorkoutIntent.DecreaseSets -> updateExercise(intent.exerciseId) { it.copy(sets = (it.sets - 1).coerceAtLeast(0)) }
            is WorkoutIntent.StartNow -> startExercise(intent.exerciseId)
            is WorkoutIntent.CompleteExercise -> completeExercise(intent.exerciseId)
            WorkoutIntent.FinishClick -> handleFinishClick()
            WorkoutIntent.ConfirmFinish -> {
                updateState { it.copy(showFinishConfirmation = false) }
                performFinish()
            }
            WorkoutIntent.DismissFinishDialog -> updateState { it.copy(showFinishConfirmation = false) }
        }
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

    private fun handleFinishClick() {
        val hasUnfinished = currentState.exercises.any { it.status != WorkoutExerciseStatus.COMPLETED }
        if (hasUnfinished) {
            updateState { it.copy(showFinishConfirmation = true) }
        } else {
            performFinish()
        }
    }

    private fun performFinish() {
        if (currentState.isSaving) return

        val finishedAt = System.currentTimeMillis()
        val workouts = currentState.exercises
            .filter { it.status == WorkoutExerciseStatus.COMPLETED }
            .map { exerciseState ->
                Workout(
                    exerciseId = exerciseState.exercise.id,
                    reps = exerciseState.reps,
                    sets = exerciseState.sets,
                    workoutDate = finishedAt,
                    totalDurationMillis = finishedAt - currentState.workoutStartedAtMillis,
                    exerciseDurationMillis = exerciseState.durationMillis ?: 0L
                )
            }

        if (workouts.isEmpty()) {
            sendEffect(WorkoutEffect.NavigateBack)
            return
        }

        updateState { it.copy(isSaving = true) }
        scope.launch {
            repository.insertWorkouts(workouts)
            updateState { it.copy(isSaving = false) }
            sendEffect(WorkoutEffect.NavigateBack)
        }
    }

    private fun updateExercise(
        exerciseId: Long,
        transform: (WorkoutExerciseState) -> WorkoutExerciseState
    ) {
        updateState { state ->
            state.copy(
                exercises = state.exercises.map { exerciseState ->
                    if (exerciseState.exercise.id == exerciseId) transform(exerciseState)
                    else exerciseState
                }
            )
        }
    }
}
