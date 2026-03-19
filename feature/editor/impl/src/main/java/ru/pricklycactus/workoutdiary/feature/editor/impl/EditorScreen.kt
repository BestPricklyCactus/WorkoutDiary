package ru.pricklycactus.workoutdiary.feature.editor.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.pricklycactus.workoutdiary.feature.common.Dimensions
import ru.pricklycactus.workoutdiary.feature.common.ExerciseForm
import ru.pricklycactus.workoutdiary.feature.common.ExercisesList
import ru.pricklycactus.workoutdiary.feature.editor.api.EditorIntent
import ru.pricklycactus.workoutdiary.feature.editor.api.EditorStore
import ru.pricklycactus.workoutdiary.feature.editor.api.EditorViewState

@Composable
fun EditorScreen(
    state: EditorViewState,
    store: EditorStore
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.ColumnSpacing)
    ) {
        Text(
            text = stringResource(R.string.editor_title),
            style = MaterialTheme.typography.headlineMedium
        )

        if (state.showAddExerciseForm) {
            ExerciseForm(
                exerciseName = state.exerciseName,
                exerciseDescription = state.exerciseDescription,
                onNameChange = { store.dispatch(EditorIntent.OnTextChanged(EditorFieldKeys.Name, it)) },
                onDescriptionChange = { store.dispatch(EditorIntent.OnTextChanged(EditorFieldKeys.Description, it)) },
                onSave = { store.dispatch(EditorIntent.SaveExerciseClick) },
                onCancel = { store.dispatch(EditorIntent.CancelAddExerciseClick) }
            )
        } else {
            Button(
                onClick = { store.dispatch(EditorIntent.AddExerciseClick) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.editor_add_exercise))
            }
        }

        HorizontalDivider()

        ExercisesList(
            exercises = state.exercises,
            selectedExerciseIds = state.selectedExerciseIds,
            onExerciseSelect = { id, selected ->
                store.dispatch(EditorIntent.OnExerciseSelected(id, selected))
            },
            onDeleteExercises = { ids ->
                store.dispatch(EditorIntent.OnExercisesDelete(ids))
            },
            onEditExercise = { id ->
                store.dispatch(EditorIntent.EditExerciseClick(id))
            },
            modifier = Modifier.weight(1f)
        )
    }
}
