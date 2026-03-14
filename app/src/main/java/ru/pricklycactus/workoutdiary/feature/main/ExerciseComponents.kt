package ru.pricklycactus.workoutdiary.feature.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ru.pricklycactus.workoutdiary.data.database.Exercise

@Composable
fun ExerciseForm(
    exerciseName: String,
    exerciseDescription: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = exerciseName,
            onValueChange = onNameChange,
            label = { Text("Название") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = exerciseDescription,
            onValueChange = onDescriptionChange,
            label = { Text("Описание") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onSave, modifier = Modifier.weight(1f)) {
                Text("Сохранить")
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Отмена")
            }
        }
    }
}

@Composable
fun ExercisesList(
    exercises: List<Exercise>,
    selectedExerciseIds: Set<Long>,
    onExerciseSelect: (Long, Boolean) -> Unit,
    onDeleteExercises: ((Set<Long>) -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Список упражнений", style = MaterialTheme.typography.titleLarge)
            if (onDeleteExercises != null && selectedExerciseIds.isNotEmpty()) {
                IconButton(onClick = { onDeleteExercises(selectedExerciseIds) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(exercises, key = { it.id }) { exercise ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = selectedExerciseIds.contains(exercise.id),
                            onValueChange = { onExerciseSelect(exercise.id, it) },
                            role = Role.Checkbox
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = selectedExerciseIds.contains(exercise.id),
                        onCheckedChange = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
                        if (exercise.description.isNotEmpty()) {
                            Text(exercise.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
