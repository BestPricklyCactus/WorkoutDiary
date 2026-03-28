package ru.pricklycactus.workoutdiary.feature.aiworkout.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.pricklycactus.workoutdiary.data.domain.ExerciseDomain
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiGeneratedExercise
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiWorkoutEffect
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiWorkoutIntent
import ru.pricklycactus.workoutdiary.testutil.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class AiWorkoutStoreImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mockk<WorkoutRepository>(relaxed = true)
    private val generator = mockk<LlmWorkoutGenerator>()

    @Test
    fun GivenBlankPrompt_WhenGenerateDispatched_ThenErrorIsShown() = runTest {
        // Arrange
        val store = AiWorkoutStoreImpl(repository, generator, this)

        // Act
        store.dispatch(AiWorkoutIntent.GenerateWorkoutClick)

        // Assert
        assertEquals("Опиши, какую тренировку ты хочешь", store.state.value.error)
    }

    @Test
    fun GivenValidPrompt_WhenGenerateSucceeds_ThenExercisesAppearInState() = runTest {
        // Arrange
        coEvery { generator.generateWorkout("legs") } returns GeneratedWorkoutPlan(
            title = "Leg plan",
            exercises = listOf(
                AiGeneratedExercise(
                    id = "1",
                    name = "Приседания",
                    description = "desc",
                    sets = 3,
                    reps = "10"
                )
            )
        )
        val store = AiWorkoutStoreImpl(repository, generator, this)
        store.dispatch(AiWorkoutIntent.OnPromptChanged("legs"))

        // Act
        store.dispatch(AiWorkoutIntent.GenerateWorkoutClick)
        advanceUntilIdle()

        // Assert
        assertEquals(listOf("Приседания"), store.state.value.generatedExercises.map { it.name })
    }

    @Test
    fun GivenGeneratedExercise_WhenAddExerciseDispatched_ThenRepositoryReceivesExercise() = runTest {
        // Arrange
        coEvery { generator.generateWorkout("legs") } returns GeneratedWorkoutPlan(
            title = "Leg plan",
            exercises = listOf(AiGeneratedExercise("1", "Приседания", "desc", 3, "10"))
        )
        coEvery { repository.upsertExercise(any()) } returns Unit
        val store = AiWorkoutStoreImpl(repository, generator, this)
        store.dispatch(AiWorkoutIntent.OnPromptChanged("legs"))
        store.dispatch(AiWorkoutIntent.GenerateWorkoutClick)
        advanceUntilIdle()

        // Act
        store.dispatch(AiWorkoutIntent.AddExerciseToDatabase("1"))
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { repository.upsertExercise(match<ExerciseDomain> { it.name == "Приседания" }) }
    }

    @Test
    fun GivenAlreadySavedExercises_WhenSaveAllDispatched_ThenAlreadySavedEffectIsEmitted() = runTest {
        // Arrange
        val store = AiWorkoutStoreImpl(repository, generator, this)
        store.dispatch(
            AiWorkoutIntent.OnPromptChanged("legs")
        )
        coEvery { generator.generateWorkout("legs") } returns GeneratedWorkoutPlan(
            title = "Leg plan",
            exercises = listOf(AiGeneratedExercise("1", "Приседания", "desc", 3, "10", isSaved = true))
        )
        store.dispatch(AiWorkoutIntent.GenerateWorkoutClick)
        advanceUntilIdle()
        val effectDeferred = async { store.effect.first() }

        // Act
        store.dispatch(AiWorkoutIntent.SaveAllExercisesToDatabase)

        // Assert
        assertTrue(effectDeferred.await() is AiWorkoutEffect.ShowMessage)
    }
}
