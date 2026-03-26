package ru.pricklycactus.workoutdiary.feature.report.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import ru.pricklycactus.workoutdiary.core.mvi.MviEffect
import ru.pricklycactus.workoutdiary.core.mvi.MviIntent

interface ReportStore {
    val state: StateFlow<ReportViewState>
    val effect: Flow<ReportEffect>
    fun dispatch(intent: ReportIntent)
}

sealed interface ReportIntent : MviIntent {
    data object LoadReportData : ReportIntent
    data object HideReport : ReportIntent
    data object ShareReport : ReportIntent
    data class SelectReportPeriod(val period: ReportPeriod) : ReportIntent
    data object PickCustomStartDate : ReportIntent
    data object PickCustomEndDate : ReportIntent
    data class UpdateCustomStartDate(val timestamp: Long) : ReportIntent
    data class UpdateCustomEndDate(val timestamp: Long) : ReportIntent
}

sealed interface ReportEffect : MviEffect {
    data class OpenDatePicker(
        val target: DatePickerTarget,
        val initialDateMillis: Long
    ) : ReportEffect

    data class ShareReport(
        val report: ReportTable,
        val periodLabel: String
    ) : ReportEffect
}

enum class DatePickerTarget {
    START,
    END
}
