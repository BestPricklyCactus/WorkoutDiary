package ru.pricklycactus.workoutdiary.feature.history.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.pricklycactus.workoutdiary.core.mvi.MviEffect
import ru.pricklycactus.workoutdiary.core.mvi.MviIntent

interface HistoryStore {
    val state: StateFlow<HistoryViewState>
    val effect: Flow<HistoryEffect>
    fun dispatch(intent: HistoryIntent)
}

sealed interface HistoryIntent : MviIntent {
    data object LoadHistory : HistoryIntent
    data class RequestWorkoutDeletion(val workoutDate: Long) : HistoryIntent
    data object DismissWorkoutDeletion : HistoryIntent
    data object ConfirmWorkoutDeletion : HistoryIntent
}

sealed interface HistoryEffect : MviEffect
