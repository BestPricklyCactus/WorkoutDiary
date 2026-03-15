package ru.pricklycactus.workoutdiary.core.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface MviState
interface MviIntent
interface MviEffect

abstract class MviStore<S : MviState, I : MviIntent, E : MviEffect>(
    initialState: S,
    protected val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    protected var currentState: S
        get() = _state.value
        set(value) {
            _state.value = value
        }

    abstract fun dispatch(intent: I)

    protected fun updateState(reducer: (S) -> S) {
        _state.update(reducer)
    }

    protected fun sendEffect(effect: E) {
        scope.launch {
            _effect.send(effect)
        }
    }
}
