package ru.pricklycactus.workoutdiary.feature.aiworkout.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.core.mvi.MviStore
import ru.pricklycactus.workoutdiary.data.domain.ExerciseDomain
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
            updateState { it.copy(error = AiWorkoutStrings.promptRequired) }
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
                sendEffect(AiWorkoutEffect.ShowMessage(AiWorkoutStrings.planReady))
            }.onFailure { error ->
                updateState {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: AiWorkoutStrings.generationFailed
                    )
                }
            }
        }
    }

    private fun addExerciseToDatabase(exerciseId: String) {
        val exercise = currentState.generatedExercises.firstOrNull { it.id == exerciseId } ?: return

        scope.launch {
            repository.upsertExercise(exercise.toDomainExercise())
            updateState { state ->
                state.copy(
                    generatedExercises = state.generatedExercises.map {
                        if (it.id == exerciseId) it.copy(isSaved = true) else it
                    }
                )
            }
            sendEffect(AiWorkoutEffect.ShowMessage(AiWorkoutStrings.exerciseSaved))
        }
    }

    private fun saveAllExercisesToDatabase() {
        val exercisesToSave = currentState.generatedExercises.filterNot { it.isSaved }
        if (exercisesToSave.isEmpty()) {
            sendEffect(AiWorkoutEffect.ShowMessage(AiWorkoutStrings.allExercisesSaved))
            return
        }

        scope.launch {
            exercisesToSave.forEach { repository.upsertExercise(it.toDomainExercise()) }
            updateState { state ->
                state.copy(generatedExercises = state.generatedExercises.map { it.copy(isSaved = true) })
            }
            sendEffect(AiWorkoutEffect.ShowMessage(AiWorkoutStrings.planSaved))
        }
    }

    private fun AiGeneratedExercise.toDomainExercise(): ExerciseDomain {
        val details = buildString {
            append(description)
            append("\n\nПодходы: ")
            append(sets)
            append("\nПовторения: ")
            append(reps)
        }
        return ExerciseDomain(name = name, description = details)
    }
}

private object AiWorkoutStrings {
    const val promptRequired = "Опиши, какую тренировку ты хочешь"
    const val planReady = "План тренировки готов"
    const val exerciseSaved = "Упражнение добавлено в базу"
    const val allExercisesSaved = "Все упражнения уже сохранены"
    const val planSaved = "План сохранен в базу"
    const val generationFailed = "Не удалось сгенерировать тренировку"
}
