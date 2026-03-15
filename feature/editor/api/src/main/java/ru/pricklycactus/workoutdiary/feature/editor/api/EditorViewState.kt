package ru.pricklycactus.workoutdiary.feature.editor.api

import ru.pricklycactus.workoutdiary.core.mvi.MviState
import ru.pricklycactus.workoutdiary.data.database.Exercise

data class EditorViewState(
    val showAddExerciseForm: Boolean = false,
    val editingExerciseId: Long? = null,
    val exerciseName: String = "",
    val exerciseDescription: String = "",
    val exercises: List<Exercise> = emptyList(),
    val selectedExerciseIds: Set<Long> = emptySet()
) : MviState
