package ru.pricklycactus.workoutdiary.feature.history

sealed interface HistoryUserEvent {
    data object LoadHistory : HistoryUserEvent
}
