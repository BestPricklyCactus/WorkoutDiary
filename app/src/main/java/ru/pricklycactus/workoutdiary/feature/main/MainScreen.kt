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
    onEvent: (MainUserEvent) -> Unit,
    onShowHistoryClick: () -> Unit,
    onStartWorkoutClick: () -> Unit,
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
                    onValueChange = { onEvent(MainUserEvent.OnTextChanged("name", it)) },
                    label = { Text("Название упражнения") }
                )
                OutlinedTextField(
                    value = viewState.exerciseDescription,
                    onValueChange = { onEvent(MainUserEvent.OnTextChanged("description", it)) },
                    label = { Text("Описание") }
                )
                Button(onClick = { onEvent(MainUserEvent.OnClick("save_exercise")) }) {
                    Text("Добавить")
                }
                Button(onClick = { onEvent(MainUserEvent.OnClick("cancel_add_exercise")) }) {
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
                        onClick = {
                            onEvent(MainUserEvent.OnExercisesDelete(viewState.selectedExerciseIds))
                        },
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
                                        onEvent(MainUserEvent.OnExerciseSelected(exercise.id, selected))
                                    }
                                )
                                .clickable {
                                    onEvent(
                                        MainUserEvent.OnExerciseSelected(
                                            exercise.id,
                                            !viewState.selectedExerciseIds.contains(exercise.id)
                                        )
                                    )
                                }
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

                Button(onClick = { onEvent(MainUserEvent.OnClick("back_to_main")) }) {
                    Text("Назад")
                }
            }
        } else {
            // Основной экран
            Button(onClick = { onEvent(MainUserEvent.OnClick("add_exercise")) }) {
                Text("Добавить упражнение")
            }
            Button(onClick = { onEvent(MainUserEvent.OnClick("show_exercises")) }) {
                Text("Просмотреть упражнения")
            }
            Button(onClick = onShowHistoryClick) {
                Text("История тренировок")
            }
        }
    }
}
