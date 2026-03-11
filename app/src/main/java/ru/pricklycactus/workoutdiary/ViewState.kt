package ru.pricklycactus.workoutdiary

data class ViewState(
    val searchText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)