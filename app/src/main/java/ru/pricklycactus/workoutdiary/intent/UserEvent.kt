package ru.pricklycactus.workoutdiary.intent



sealed class UserEvent {
    data class OnClick(val id: Int) : UserEvent()
    data class OnTextChanged(val text: String) : UserEvent()
 /*   data class NavigateToHome(val userId: String) : ru.pricklycactus.workoutdiary.intent.UserEvent()
    data class NavigateToProfile(val userId: String) : ru.pricklycactus.workoutdiary.intent.UserEvent()
    data class ShowError(val message: String) : ru.pricklycactus.workoutdiary.intent.UserEvent()*/
}
