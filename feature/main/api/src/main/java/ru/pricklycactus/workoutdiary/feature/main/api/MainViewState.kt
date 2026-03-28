package ru.pricklycactus.workoutdiary.feature.main.api

import ru.pricklycactus.workoutdiary.core.mvi.MviState
import ru.pricklycactus.workoutdiary.data.domain.ExerciseDomain

data class MainViewState(
    val searchText: String = "",
    val showAddExerciseForm: Boolean = false,
    val exerciseName: String = "",
    val exerciseDescription: String = "",
    val showExercisesList: Boolean = false,
    val exercises: List<ExerciseDomain> = emptyList(),
    val selectedExerciseIds: Set<Long> = emptySet()
) : MviState
