package ru.pricklycactus.workoutdiary.feature.report.api

import ru.pricklycactus.workoutdiary.core.mvi.MviState

data class ReportViewState(
    val selectedReportPeriod: ReportPeriod = ReportPeriod.WEEK,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val report: ReportTable = ReportTable()
) : MviState

enum class ReportPeriod {
    WEEK,
    MONTH,
    THREE_MONTHS,
    HALF_YEAR,
    CUSTOM
}

data class ReportTable(
    val columns: List<ReportColumn> = emptyList(),
    val rows: List<ReportRow> = emptyList(),
    val hasData: Boolean = false
)

data class ReportColumn(
    val workoutDate: Long,
    val label: String
)

data class ReportRow(
    val exerciseName: String,
    val values: List<String>
)
