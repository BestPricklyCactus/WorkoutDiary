package ru.pricklycactus.workoutdiary.feature.history

import ru.pricklycactus.workoutdiary.core.mvi.MviEffect
import ru.pricklycactus.workoutdiary.core.mvi.MviIntent

sealed interface HistoryIntent : MviIntent {
    data object LoadHistory : HistoryIntent
}

sealed interface HistoryEffect : MviEffect
