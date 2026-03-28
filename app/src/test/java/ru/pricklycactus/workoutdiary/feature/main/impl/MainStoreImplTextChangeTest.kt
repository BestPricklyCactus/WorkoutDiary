package ru.pricklycactus.workoutdiary.feature.main.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.pricklycactus.workoutdiary.feature.main.api.MainIntent
import ru.pricklycactus.workoutdiary.testutil.MainDispatcherRule
import ru.pricklycactus.workoutdiary.testutil.createWorkoutRepository

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(Parameterized::class)
class MainStoreImplTextChangeTest(
    private val field: String,
    private val text: String,
    private val selector: (ru.pricklycactus.workoutdiary.feature.main.api.MainViewState) -> String
) {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun GivenTextChangedIntent_WhenDispatch_ThenExpectedFieldIsUpdated() = runTest {
        // Arrange
        val store = MainStoreImpl(createWorkoutRepository(), this)

        // Act
        store.dispatch(MainIntent.OnTextChanged(field, text))
        advanceUntilIdle()

        // Assert
        assertEquals(text, selector(store.state.value))
        coroutineContext.cancelChildren()
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "field={0}")
        fun data(): List<Array<Any>> = listOf(
            arrayOf(
                "name",
                "Squat",
                { state: ru.pricklycactus.workoutdiary.feature.main.api.MainViewState -> state.exerciseName }
            ),
            arrayOf(
                "description",
                "Leg day",
                { state: ru.pricklycactus.workoutdiary.feature.main.api.MainViewState -> state.exerciseDescription }
            )
        )
    }
}
