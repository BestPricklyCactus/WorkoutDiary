package ru.pricklycactus.workoutdiary.feature.workout.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.database.Workout
import ru.pricklycactus.workoutdiary.data.domain.ExerciseDomain
import ru.pricklycactus.workoutdiary.data.model.WorkoutWithExercise
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutEffect
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutIntent
import ru.pricklycactus.workoutdiary.testutil.MainDispatcherRule
import ru.pricklycactus.workoutdiary.testutil.createWorkoutRepository

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutStoreImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun GivenLastWorkoutExists_WhenStoreInitialized_ThenLastSetsAndRepsAreLoaded() = runTest {
        val exercise = ExerciseDomain(id = 1, name = "Squat", description = "desc")
        val repository = createWorkoutRepository(
            workouts = listOf(
                WorkoutWithExercise(
                    workout = Workout(
                        id = 1,
                        exerciseId = 1,
                        reps = 8,
                        sets = 3,
                        workoutDate = 1_000,
                        totalDurationMillis = 10_000,
                        exerciseDurationMillis = 2_000
                    ),
                    exercise = Exercise(id = 1, name = "Squat", description = "desc")
                )
            )
        )

        // Act
        val store = WorkoutStoreImpl(listOf(exercise), repository, this)
        advanceUntilIdle()

        // Assert
        assertEquals(8, store.state.value.exercises.first().reps)
    }

    @Test
    fun GivenLastWorkoutExists_WhenStoreInitialized_ThenLastSetsAreLoaded() = runTest {
        // Arrange
        val exercise = ExerciseDomain(id = 1, name = "Squat", description = "desc")
        val repository = createWorkoutRepository(
            workouts = listOf(
                WorkoutWithExercise(
                    workout = Workout(
                        id = 1,
                        exerciseId = 1,
                        reps = 8,
                        sets = 3,
                        workoutDate = 1_000,
                        totalDurationMillis = 10_000,
                        exerciseDurationMillis = 2_000
                    ),
                    exercise = Exercise(id = 1, name = "Squat", description = "desc")
                )
            )
        )

        // Act
        val store = WorkoutStoreImpl(listOf(exercise), repository, this)
        advanceUntilIdle()

        // Assert
        assertEquals(3, store.state.value.exercises.first().sets)
    }

    @Test
    fun GivenCompletedWorkout_WhenFinishClicked_ThenNavigateBackEffectIsEmitted() = runTest {
        // Arrange
        val exercise = ExerciseDomain(id = 1, name = "Squat", description = "desc")
        val repository = createWorkoutRepository()
        val store = WorkoutStoreImpl(listOf(exercise), repository, this)
        val effectDeferred = async { store.effect.first() }

        // Act
        store.dispatch(WorkoutIntent.StartNow(1))
        store.dispatch(WorkoutIntent.CompleteExercise(1))
        store.dispatch(WorkoutIntent.FinishClick)
        advanceUntilIdle()

        // Assert
        assertTrue(effectDeferred.await() is WorkoutEffect.NavigateBack)
    }

    @Test
    fun GivenCompletedWorkout_WhenFinishClicked_ThenWorkoutIsSaved() = runTest {
        // Arrange
        val exercise = ExerciseDomain(id = 1, name = "Squat", description = "desc")
        val repository = createWorkoutRepository()
        val store = WorkoutStoreImpl(listOf(exercise), repository, this)

        // Act
        store.dispatch(WorkoutIntent.StartNow(1))
        store.dispatch(WorkoutIntent.CompleteExercise(1))
        store.dispatch(WorkoutIntent.FinishClick)
        advanceUntilIdle()

        // Assert
        assertTrue(repository.getAllWorkouts().first().isNotEmpty())
    }
}
