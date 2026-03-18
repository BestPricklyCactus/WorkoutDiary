package ru.pricklycactus.workoutdiary.feature.aiworkout.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.core.mvi.MviStore
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiGeneratedExercise
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiWorkoutEffect
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiWorkoutIntent
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiWorkoutStore
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiWorkoutViewState

class AiWorkoutStoreImpl(
    private val repository: WorkoutRepository,
    private val generator: LlmWorkoutGenerator,
    scope: CoroutineScope
) : MviStore<AiWorkoutViewState, AiWorkoutIntent, AiWorkoutEffect>(AiWorkoutViewState(), scope),
    AiWorkoutStore {

    override fun dispatch(intent: AiWorkoutIntent) {
        when (intent) {
            is AiWorkoutIntent.OnPromptChanged -> updateState {
                it.copy(prompt = intent.value, error = null)
            }
            AiWorkoutIntent.GenerateWorkoutClick -> generateWorkout()
            is AiWorkoutIntent.AddExerciseToDatabase -> addExerciseToDatabase(intent.exerciseId)
            AiWorkoutIntent.SaveAllExercisesToDatabase -> saveAllExercisesToDatabase()
            AiWorkoutIntent.ClearError -> updateState { it.copy(error = null) }
        }
    }

    private fun generateWorkout() {
        val prompt = currentState.prompt.trim()
        if (prompt.isBlank()) {
            updateState { it.copy(error = "Опиши, какую тренировку ты хочешь") }
            return
        }

        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            runCatching {
                generator.generateWorkout(prompt)
            }.onSuccess { plan ->
                updateState {
                    it.copy(
                        isLoading = false,
                        generatedTitle = plan.title,
                        generatedExercises = plan.exercises
                    )
                }
                sendEffect(AiWorkoutEffect.ShowMessage("План тренировки готов"))
            }.onFailure { error ->
                updateState {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Не удалось сгенерировать тренировку"
                    )
                }
            }
        }
    }

    private fun addExerciseToDatabase(exerciseId: String) {
        val exercise = currentState.generatedExercises.firstOrNull { it.id == exerciseId } ?: return

        scope.launch {
            repository.upsertExercise(exercise.toDatabaseExercise())
            updateState { state ->
                state.copy(
                    generatedExercises = state.generatedExercises.map {
                        if (it.id == exerciseId) it.copy(isSaved = true) else it
                    }
                )
            }
            sendEffect(AiWorkoutEffect.ShowMessage("Упражнение добавлено в базу"))
        }
    }

    private fun saveAllExercisesToDatabase() {
        val exercisesToSave = currentState.generatedExercises.filterNot { it.isSaved }
        if (exercisesToSave.isEmpty()) {
            sendEffect(AiWorkoutEffect.ShowMessage("Все упражнения уже сохранены"))
            return
        }

        scope.launch {
            exercisesToSave.forEach { repository.upsertExercise(it.toDatabaseExercise()) }
            updateState { state ->
                state.copy(generatedExercises = state.generatedExercises.map { it.copy(isSaved = true) })
            }
            sendEffect(AiWorkoutEffect.ShowMessage("План сохранен в базу"))
        }
    }

    private fun AiGeneratedExercise.toDatabaseExercise(): Exercise {
        val details = buildString {
            append(description)
            append("\n\nПодходы: ")
            append(sets)
            append("\nПовторения: ")
            append(reps)
        }
        return Exercise(name = name, description = details)
    }

}
