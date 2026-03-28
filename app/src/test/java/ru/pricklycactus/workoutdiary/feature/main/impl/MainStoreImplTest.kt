package ru.pricklycactus.workoutdiary.feature.main.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.feature.main.api.MainIntent
import ru.pricklycactus.workoutdiary.testutil.MainDispatcherRule
import ru.pricklycactus.workoutdiary.testutil.createWorkoutRepository

@OptIn(ExperimentalCoroutinesApi::class)
class MainStoreImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun GivenRepositoryExercises_WhenStoreInitialized_ThenExercisesLoadedIntoState() = runTest {
        // Arrange
        val repository = createWorkoutRepository(
            exercises = listOf(
                Exercise(id = 1, name = "Squat", description = "Leg exercise"),
                Exercise(id = 2, name = "Bench", description = "Chest exercise")
            )
        )

        // Act
        val store = MainStoreImpl(repository, this)
        advanceUntilIdle()

        // Assert
        assertEquals(listOf(1L, 2L), store.state.value.exercises.map { it.id })
        coroutineContext.cancelChildren()
    }

    @Test
    fun GivenSelectedExercise_WhenDeleteDispatched_ThenSelectionIsCleared() = runTest {
        // Arrange
        val repository = createWorkoutRepository(
            exercises = listOf(
                Exercise(id = 1, name = "Squat", description = "Leg exercise"),
                Exercise(id = 2, name = "Bench", description = "Chest exercise")
            )
        )
        val store = MainStoreImpl(repository, this)
        advanceUntilIdle()
        store.dispatch(MainIntent.OnExerciseSelected(1, true))

        // Act
        store.dispatch(MainIntent.OnExercisesDelete(setOf(1)))
        advanceUntilIdle()

        // Assert
        assertTrue(store.state.value.selectedExerciseIds.isEmpty())
        coroutineContext.cancelChildren()
    }

    @Test
    fun GivenDeletedExercise_WhenDeleteDispatched_ThenExerciseRemovedFromState() = runTest {
        // Arrange
        val repository = createWorkoutRepository(
            exercises = listOf(
                Exercise(id = 1, name = "Squat", description = "Leg exercise"),
                Exercise(id = 2, name = "Bench", description = "Chest exercise")
            )
        )
        val store = MainStoreImpl(repository, this)
        advanceUntilIdle()

        // Act
        store.dispatch(MainIntent.OnExercisesDelete(setOf(1)))
        advanceUntilIdle()

        // Assert
        assertEquals(listOf(2L), store.state.value.exercises.map { it.id })
        coroutineContext.cancelChildren()
    }
}
