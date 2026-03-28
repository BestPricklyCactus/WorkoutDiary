package ru.pricklycactus.workoutdiary.feature.report.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.core.mvi.MviStore
import ru.pricklycactus.workoutdiary.data.domain.WorkoutWithExerciseDomain
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository
import ru.pricklycactus.workoutdiary.feature.report.api.DatePickerTarget
import ru.pricklycactus.workoutdiary.feature.report.api.ReportColumn
import ru.pricklycactus.workoutdiary.feature.report.api.ReportEffect
import ru.pricklycactus.workoutdiary.feature.report.api.ReportIntent
import ru.pricklycactus.workoutdiary.feature.report.api.ReportPeriod
import ru.pricklycactus.workoutdiary.feature.report.api.ReportRow
import ru.pricklycactus.workoutdiary.feature.report.api.ReportStore
import ru.pricklycactus.workoutdiary.feature.report.api.ReportTable
import ru.pricklycactus.workoutdiary.feature.report.api.ReportViewState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReportStoreImpl(
    private val repository: WorkoutRepository,
    scope: CoroutineScope
) : MviStore<ReportViewState, ReportIntent, ReportEffect>(ReportViewState(), scope), ReportStore {

    private var allWorkouts: List<WorkoutWithExerciseDomain> = emptyList()

    init {
        dispatch(ReportIntent.LoadReportData)
    }

    override fun dispatch(intent: ReportIntent) {
        when (intent) {
            ReportIntent.LoadReportData -> observeHistory()
            ReportIntent.HideReport -> Unit
            ReportIntent.ShareReport -> {
                if (currentState.report.hasData) {
                    sendEffect(ReportEffect.ShareReport(currentState.report, currentState.selectedReportPeriod.label))
                }
            }
            is ReportIntent.SelectReportPeriod -> {
                updateState { it.copy(selectedReportPeriod = intent.period) }
                refreshReport()
            }
            ReportIntent.PickCustomStartDate -> sendEffect(
                ReportEffect.OpenDatePicker(DatePickerTarget.START, currentState.customStartDate ?: System.currentTimeMillis())
            )
            ReportIntent.PickCustomEndDate -> sendEffect(
                ReportEffect.OpenDatePicker(DatePickerTarget.END, currentState.customEndDate ?: System.currentTimeMillis())
            )
            is ReportIntent.UpdateCustomStartDate -> {
                updateState { it.copy(customStartDate = startOfDay(intent.timestamp)) }
                refreshReport()
            }
            is ReportIntent.UpdateCustomEndDate -> {
                updateState { it.copy(customEndDate = endOfDay(intent.timestamp)) }
                refreshReport()
            }
        }
    }

    private fun observeHistory() {
        scope.launch {
            repository.getWorkoutsWithExercises().collectLatest {
                allWorkouts = it
                refreshReport()
            }
        }
    }

    private fun refreshReport() {
        val range = resolveRange(currentState.selectedReportPeriod, currentState.customStartDate, currentState.customEndDate)
        val filtered = allWorkouts.filter { range == null || it.workout.date in range.first..range.second }
        val columns = filtered.map { it.workout.date }.distinct().sorted().map { ReportColumn(it, reportDateFormatter.format(Date(it))) }
        val rows = filtered.groupBy { it.exercise.name }
            .toSortedMap(String.CASE_INSENSITIVE_ORDER)
            .map { (exerciseName, items) ->
                val valuesByDate = items.associateBy({ it.workout.date }, { "${it.workout.reps}/${it.workout.sets}" })
                ReportRow(exerciseName, columns.map { column -> valuesByDate[column.workoutDate] ?: dashValue })
            }
        updateState { it.copy(report = ReportTable(columns, rows, columns.isNotEmpty() && rows.isNotEmpty())) }
    }

    private fun resolveRange(period: ReportPeriod, customStart: Long?, customEnd: Long?): Pair<Long, Long>? {
        val now = System.currentTimeMillis()
        return when (period) {
            ReportPeriod.WEEK -> startOfDay(daysAgo(now, 6)) to endOfDay(now)
            ReportPeriod.MONTH -> startOfDay(monthsAgo(now, 1)) to endOfDay(now)
            ReportPeriod.THREE_MONTHS -> startOfDay(monthsAgo(now, 3)) to endOfDay(now)
            ReportPeriod.HALF_YEAR -> startOfDay(monthsAgo(now, 6)) to endOfDay(now)
            ReportPeriod.CUSTOM -> if (customStart != null && customEnd != null) minOf(customStart, customEnd) to maxOf(customStart, customEnd) else null
        }
    }

    private fun daysAgo(base: Long, days: Int): Long = Calendar.getInstance().run { timeInMillis = base; add(Calendar.DAY_OF_YEAR, -days); timeInMillis }
    private fun monthsAgo(base: Long, months: Int): Long = Calendar.getInstance().run { timeInMillis = base; add(Calendar.MONTH, -months); timeInMillis }
    private fun startOfDay(value: Long): Long = Calendar.getInstance().run { timeInMillis = value; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); timeInMillis }
    private fun endOfDay(value: Long): Long = Calendar.getInstance().run { timeInMillis = value; set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999); timeInMillis }

    private companion object {
        val reportDateFormatter = SimpleDateFormat("dd.MM", Locale.getDefault())
        const val dashValue = "-"
    }
}

private val ReportPeriod.label: String
    get() = when (this) {
        ReportPeriod.WEEK -> "week"
        ReportPeriod.MONTH -> "month"
        ReportPeriod.THREE_MONTHS -> "three_months"
        ReportPeriod.HALF_YEAR -> "half_year"
        ReportPeriod.CUSTOM -> "custom"
    }
