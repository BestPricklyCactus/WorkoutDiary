package ru.pricklycactus.workoutdiary.feature.main

import ru.pricklycactus.workoutdiary.data.database.Exercise

data class MainViewState(
    val searchText: String = "",
    val showAddExerciseForm: Boolean = false,
    val exerciseName: String = "",
    val exerciseDescription: String = "",
    val showExercisesList: Boolean = false,
    val exercises: List<Exercise> = emptyList(),
    val selectedExerciseIds: Set<Long> = emptySet()
)
