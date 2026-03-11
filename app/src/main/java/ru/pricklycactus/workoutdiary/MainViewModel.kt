package ru.pricklycactus.workoutdiary


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.pricklycactus.workoutdiary.intent.UserEvent

class MainViewModel : ViewModel() {
    private val _viewState = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> = _viewState

    fun processIntent(intent: UserEvent) {
        when (intent) {
            is UserEvent.OnClick -> {
                // Обработка клика
            }
            is UserEvent.OnTextChanged -> {
                _viewState.value = _viewState.value?.copy(searchText = intent.text)
            }
        }
    }
}
