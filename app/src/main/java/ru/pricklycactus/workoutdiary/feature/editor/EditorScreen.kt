package ru.pricklycactus.workoutdiary.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.pricklycactus.workoutdiary.feature.main.ExerciseForm
import ru.pricklycactus.workoutdiary.feature.main.ExercisesList

@Composable
fun EditorScreen(
    viewState: EditorViewState,
    onEvent: (EditorUserEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Редактор упражнений", style = MaterialTheme.typography.headlineMedium)

        if (viewState.showAddExerciseForm) {
            ExerciseForm(
                exerciseName = viewState.exerciseName,
                exerciseDescription = viewState.exerciseDescription,
                onNameChange = { onEvent(EditorUserEvent.OnTextChanged("name", it)) },
                onDescriptionChange = { onEvent(EditorUserEvent.OnTextChanged("description", it)) },
                onSave = { onEvent(EditorUserEvent.SaveExerciseClick) },
                onCancel = { onEvent(EditorUserEvent.CancelAddExerciseClick) }
            )
        } else {
            Button(
                onClick = { onEvent(EditorUserEvent.AddExerciseClick) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Добавить упражнение")
            }
        }

        Divider()

        ExercisesList(
            exercises = viewState.exercises,
            selectedExerciseIds = viewState.selectedExerciseIds,
            onExerciseSelect = { id, selected ->
                onEvent(EditorUserEvent.OnExerciseSelected(id, selected))
            },
            onDeleteExercises = { ids ->
                onEvent(EditorUserEvent.OnExercisesDelete(ids))
            }
        )
    }
}
