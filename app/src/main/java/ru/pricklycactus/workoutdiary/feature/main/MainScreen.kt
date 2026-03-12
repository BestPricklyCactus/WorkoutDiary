package ru.pricklycactus.workoutdiary.feature.main

import androidx.compose.foundation.clickable
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

@Composable
fun MainScreen(
    viewState: MainViewState,
    onAddExerciseClick: () -> Unit,
    onShowExercisesClick: () -> Unit,
    onShowHistoryClick: () -> Unit,
    onSaveExerciseClick: () -> Unit,
    onCancelAddExerciseClick: () -> Unit,
    onExerciseNameChange: (String) -> Unit,
    onExerciseDescriptionChange: (String) -> Unit,
    onExerciseSelected: (Long, Boolean) -> Unit,
    onDeleteSelectedExercisesClick: () -> Unit,
    onStartWorkoutClick: () -> Unit,
    onBackToHome: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewState.showAddExerciseForm) {
            // Форма добавления упражнения
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = viewState.exerciseName,
                    onValueChange = onExerciseNameChange,
                    label = { Text("Название упражнения") }
                )
                OutlinedTextField(
                    value = viewState.exerciseDescription,
                    onValueChange = onExerciseDescriptionChange,
                    label = { Text("Описание") }
                )
                Button(onClick = onSaveExerciseClick) {
                    Text("Добавить")
                }
                Button(onClick = onCancelAddExerciseClick) {
                    Text("Отмена")
                }
            }
        } else if (viewState.showExercisesList) {
            // Список упражнений
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Выберите упражнения для тренировки",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(
                        onClick = onDeleteSelectedExercisesClick,
                        enabled = viewState.selectedExerciseIds.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Удалить выбранные упражнения"
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(viewState.exercises) { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = exercise.id in viewState.selectedExerciseIds,
                                    role = Role.Checkbox,
                                    onValueChange = { selected ->
                                        onExerciseSelected(exercise.id, selected)
                                    }
                                )
                                .clickable { onExerciseSelected(exercise.id, !viewState.selectedExerciseIds.contains(exercise.id)) }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = exercise.id in viewState.selectedExerciseIds,
                                onCheckedChange = null // Управляем через toggleable
                            )
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = exercise.name, style = MaterialTheme.typography.bodyLarge)
                                Text(text = exercise.description, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                Button(
                    onClick = onStartWorkoutClick,
                    enabled = viewState.selectedExerciseIds.isNotEmpty()
                ) {
                    Text("Начать тренировку")
                }

                Button(onClick = onBackToHome) {
                    Text("Назад")
                }
            }
        } else {
            // Основной экран
            Button(onClick = onAddExerciseClick) {
                Text("Добавить упражнение")
            }
            Button(onClick = onShowExercisesClick) {
                Text("Просмотреть упражнения")
            }
            Button(onClick = onShowHistoryClick) {
                Text("История тренировок")
            }
        }
    }
}
