package ru.pricklycactus.workoutdiary.feature.report.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.database.Workout
import ru.pricklycactus.workoutdiary.data.model.WorkoutWithExercise
import ru.pricklycactus.workoutdiary.feature.report.api.ReportEffect
import ru.pricklycactus.workoutdiary.feature.report.api.ReportIntent
import ru.pricklycactus.workoutdiary.feature.report.api.ReportPeriod
import ru.pricklycactus.workoutdiary.testutil.MainDispatcherRule
import ru.pricklycactus.workoutdiary.testutil.createWorkoutRepository

@OptIn(ExperimentalCoroutinesApi::class)
class ReportStoreImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun GivenCustomRange_WhenRangeIncludesOneWorkoutDate_ThenReportContainsSingleColumn() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val repository = createWorkoutRepository(
            workouts = listOf(
                reportEntry(now - 1_000, "Squat", 3, 8),
                reportEntry(now - 86_400_000L * 10, "Bench", 4, 10)
            )
        )
        val store = ReportStoreImpl(repository, this)
        advanceUntilIdle()

        // Act
        store.dispatch(ReportIntent.SelectReportPeriod(ReportPeriod.CUSTOM))
        store.dispatch(ReportIntent.UpdateCustomStartDate(now - 2_000))
        store.dispatch(ReportIntent.UpdateCustomEndDate(now))
        advanceUntilIdle()

        // Assert
        assertEquals(1, store.state.value.report.columns.size)
        coroutineContext.cancelChildren()
    }

    @Test
    fun GivenCustomRange_WhenRangeIncludesWorkout_ThenReportContainsRepsAndSetsValue() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val repository = createWorkoutRepository(
            workouts = listOf(reportEntry(now - 1_000, "Squat", 3, 8))
        )
        val store = ReportStoreImpl(repository, this)
        advanceUntilIdle()

        // Act
        store.dispatch(ReportIntent.SelectReportPeriod(ReportPeriod.CUSTOM))
        store.dispatch(ReportIntent.UpdateCustomStartDate(now - 2_000))
        store.dispatch(ReportIntent.UpdateCustomEndDate(now))
        advanceUntilIdle()

        // Assert
        assertEquals("8/3", store.state.value.report.rows.single().values.single())
        coroutineContext.cancelChildren()
    }

    @Test
    fun GivenReportWithData_WhenShareDispatched_ThenShareEffectIsEmitted() = runTest {
        // Arrange
        val now = System.currentTimeMillis()
        val repository = createWorkoutRepository(
            workouts = listOf(reportEntry(now - 1_000, "Squat", 3, 8))
        )
        val store = ReportStoreImpl(repository, this)
        advanceUntilIdle()
        store.dispatch(ReportIntent.SelectReportPeriod(ReportPeriod.CUSTOM))
        store.dispatch(ReportIntent.UpdateCustomStartDate(now - 2_000))
        store.dispatch(ReportIntent.UpdateCustomEndDate(now))
        advanceUntilIdle()
        val effectDeferred = async { store.effect.first() }

        // Act
        store.dispatch(ReportIntent.ShareReport)

        // Assert
        assertTrue(effectDeferred.await() is ReportEffect.ShareReport)
        coroutineContext.cancelChildren()
    }

    private fun reportEntry(date: Long, name: String, sets: Int, reps: Int) = WorkoutWithExercise(
        workout = Workout(
            id = date,
            exerciseId = date,
            reps = reps,
            sets = sets,
            workoutDate = date,
            totalDurationMillis = 1_000,
            exerciseDurationMillis = 500
        ),
        exercise = Exercise(id = date, name = name, description = "desc")
    )
}
