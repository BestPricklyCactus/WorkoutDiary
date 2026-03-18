package ru.pricklycactus.workoutdiary.feature.aiworkout.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.pricklycactus.workoutdiary.core.mvi.MviEffect
import ru.pricklycactus.workoutdiary.core.mvi.MviIntent

interface AiWorkoutStore {
    val state: StateFlow<AiWorkoutViewState>
    val effect: Flow<AiWorkoutEffect>
    fun dispatch(intent: AiWorkoutIntent)
}

sealed interface AiWorkoutIntent : MviIntent {
    data class OnPromptChanged(val value: String) : AiWorkoutIntent
    data object GenerateWorkoutClick : AiWorkoutIntent
    data class AddExerciseToDatabase(val exerciseId: String) : AiWorkoutIntent
    data object SaveAllExercisesToDatabase : AiWorkoutIntent
    data object ClearError : AiWorkoutIntent
}

sealed interface AiWorkoutEffect : MviEffect {
    data class ShowMessage(val message: String) : AiWorkoutEffect
}
