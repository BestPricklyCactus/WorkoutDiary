package ru.pricklycactus.workoutdiary.ui

data class MainViewState(
    val searchText: String = "",
    val showAddExerciseForm: Boolean = false,
    val exerciseName: String = "",
    val exerciseDescription: String = ""
)