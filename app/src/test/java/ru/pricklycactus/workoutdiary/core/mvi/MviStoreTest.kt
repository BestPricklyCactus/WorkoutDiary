package ru.pricklycactus.workoutdiary.core.mvi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.pricklycactus.workoutdiary.testutil.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class MviStoreTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun GivenIncrementIntent_WhenDispatch_ThenStateCountIncreases() = runTest {
        // Arrange
        val store = TestCounterStore(this)

        // Act
        store.dispatch(TestIntent.Increment)

        // Assert
        assertEquals(1, store.state.value.count)
    }

    @Test
    fun GivenNotifyIntent_WhenDispatch_ThenEffectIsEmitted() = runTest {
        // Arrange
        val store = TestCounterStore(this)
        val effectDeferred = async { store.effect.first() }

        // Act
        store.dispatch(TestIntent.Notify)

        // Assert
        assertEquals(TestEffect.Notified, effectDeferred.await())
    }

    private data class TestState(val count: Int = 0) : MviState

    private sealed interface TestIntent : MviIntent {
        data object Increment : TestIntent
        data object Notify : TestIntent
    }

    private sealed interface TestEffect : MviEffect {
        data object Notified : TestEffect
    }

    private class TestCounterStore(scope: kotlinx.coroutines.CoroutineScope) :
        MviStore<TestState, TestIntent, TestEffect>(TestState(), scope) {
        override fun dispatch(intent: TestIntent) {
            when (intent) {
                TestIntent.Increment -> updateState { it.copy(count = it.count + 1) }
                TestIntent.Notify -> sendEffect(TestEffect.Notified)
            }
        }
    }
}
