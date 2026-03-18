package ru.pricklycactus.workoutdiary.feature.aiworkout.api

import ru.pricklycactus.workoutdiary.core.mvi.MviState

data class AiGeneratedExercise(
    val id: String,
    val name: String,
    val description: String,
    val sets: Int,
    val reps: String,
    val isSaved: Boolean = false
)

data class AiWorkoutViewState(
    val prompt: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val generatedTitle: String = "",
    val generatedExercises: List<AiGeneratedExercise> = emptyList()
) : MviState
