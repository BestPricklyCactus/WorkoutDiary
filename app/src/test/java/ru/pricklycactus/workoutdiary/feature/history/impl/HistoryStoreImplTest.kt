package ru.pricklycactus.workoutdiary.feature.history.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.database.Workout
import ru.pricklycactus.workoutdiary.data.model.WorkoutWithExercise
import ru.pricklycactus.workoutdiary.testutil.MainDispatcherRule
import ru.pricklycactus.workoutdiary.testutil.createWorkoutRepository

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryStoreImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun GivenMultipleWorkoutDates_WhenStoreInitialized_ThenHistorySortedDescendingByDate() = runTest {
        // Arrange
        val repository = createWorkoutRepository(
            workouts = listOf(
                workoutWithExercise(1_000L, "Squat", 3, 8),
                workoutWithExercise(2_000L, "Bench", 4, 10),
                workoutWithExercise(2_000L, "Row", 3, 12)
            )
        )

        // Act
        val store = HistoryStoreImpl(repository, this)
        advanceUntilIdle()

        // Assert
        assertEquals(listOf(2_000L, 1_000L), store.state.value.workouts.map { it.workoutDate })
        coroutineContext.cancelChildren()
    }

    @Test
    fun GivenWorkoutEntriesWithSameDate_WhenStoreInitialized_ThenExercisesGroupedIntoSingleHistoryEntry() = runTest {
        // Arrange
        val repository = createWorkoutRepository(
            workouts = listOf(
                workoutWithExercise(2_000L, "Bench", 4, 10),
                workoutWithExercise(2_000L, "Row", 3, 12)
            )
        )

        // Act
        val store = HistoryStoreImpl(repository, this)
        advanceUntilIdle()

        // Assert
        assertEquals(listOf("Bench", "Row"), store.state.value.workouts.single().exercises.map { it.exerciseName })
        coroutineContext.cancelChildren()
    }

    private fun workoutWithExercise(date: Long, name: String, sets: Int, reps: Int) =
        WorkoutWithExercise(
            workout = Workout(
                id = date,
                exerciseId = date,
                reps = reps,
                sets = sets,
                workoutDate = date,
                totalDurationMillis = 600_000,
                exerciseDurationMillis = 120_000
            ),
            exercise = Exercise(id = date, name = name, description = "desc")
        )
}
