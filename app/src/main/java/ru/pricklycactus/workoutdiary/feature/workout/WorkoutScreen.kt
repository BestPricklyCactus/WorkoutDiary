package ru.pricklycactus.workoutdiary.feature.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WorkoutScreen(
    state: WorkoutViewState,
    store: WorkoutStore,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        store.effect.collectLatest { effect ->
            when (effect) {
                WorkoutEffect.NavigateBack -> onBack()
            }
        }
    }

    if (state.showFinishConfirmation) {
        AlertDialog(
            onDismissRequest = { store.dispatch(WorkoutIntent.DismissFinishDialog) },
            title = { Text("Завершить тренировку?") },
            text = { Text("У вас есть неначатые упражнения. Вы уверены, что хотите завершить тренировку?") },
            confirmButton = {
                TextButton(onClick = { store.dispatch(WorkoutIntent.ConfirmFinish) }) {
                    Text("Да")
                }
            },
            dismissButton = {
                TextButton(onClick = { store.dispatch(WorkoutIntent.DismissFinishDialog) }) {
                    Text("Нет")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Тренировка", style = MaterialTheme.typography.headlineMedium)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.exercises, key = { it.exercise.id }) { exerciseState ->
                ExerciseItem(
                    exerciseState = exerciseState,
                    onStartClick = { store.dispatch(WorkoutIntent.StartNow(exerciseState.exercise.id)) },
                    onCompleteClick = { store.dispatch(WorkoutIntent.CompleteExercise(exerciseState.exercise.id)) },
                    onIncreaseSets = { store.dispatch(WorkoutIntent.IncreaseSets(exerciseState.exercise.id)) },
                    onDecreaseSets = { store.dispatch(WorkoutIntent.DecreaseSets(exerciseState.exercise.id)) },
                    onIncreaseReps = { store.dispatch(WorkoutIntent.IncreaseReps(exerciseState.exercise.id)) },
                    onDecreaseReps = { store.dispatch(WorkoutIntent.DecreaseReps(exerciseState.exercise.id)) }
                )
            }
        }

        Button(
            onClick = { store.dispatch(WorkoutIntent.FinishClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving
        ) {
            Text(if (state.isSaving) "Сохранение..." else "Завершить тренировку")
        }
    }
}

@Composable
fun ExerciseItem(
    exerciseState: WorkoutExerciseState,
    onStartClick: () -> Unit,
    onCompleteClick: () -> Unit,
    onIncreaseSets: () -> Unit,
    onDecreaseSets: () -> Unit,
    onIncreaseReps: () -> Unit,
    onDecreaseReps: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (exerciseState.status) {
                WorkoutExerciseStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
                WorkoutExerciseStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
                WorkoutExerciseStatus.IDLE -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = exerciseState.exercise.name,
                style = MaterialTheme.typography.titleLarge
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Подходы: ", style = MaterialTheme.typography.bodyMedium)
                IconButton(onClick = onDecreaseSets, enabled = exerciseState.status != WorkoutExerciseStatus.COMPLETED) {
                    Icon(Icons.Default.Remove, contentDescription = "Меньше")
                }
                Text(exerciseState.sets.toString(), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = onIncreaseSets, enabled = exerciseState.status != WorkoutExerciseStatus.COMPLETED) {
                    Icon(Icons.Default.Add, contentDescription = "Больше")
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Повторения: ", style = MaterialTheme.typography.bodyMedium)
                IconButton(onClick = onDecreaseReps, enabled = exerciseState.status != WorkoutExerciseStatus.COMPLETED) {
                    Icon(Icons.Default.Remove, contentDescription = "Меньше")
                }
                Text(exerciseState.reps.toString(), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = onIncreaseReps, enabled = exerciseState.status != WorkoutExerciseStatus.COMPLETED) {
                    Icon(Icons.Default.Add, contentDescription = "Больше")
                }
            }

            if (exerciseState.status == WorkoutExerciseStatus.COMPLETED && exerciseState.durationMillis != null) {
                Text(
                    text = "Время выполнения: ${formatDuration(exerciseState.durationMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when (exerciseState.status) {
                    WorkoutExerciseStatus.IDLE -> {
                        Button(onClick = onStartClick) {
                            Text("Начать")
                        }
                    }
                    WorkoutExerciseStatus.IN_PROGRESS -> {
                        Button(onClick = onCompleteClick) {
                            Text("Завершить")
                        }
                    }
                    WorkoutExerciseStatus.COMPLETED -> {
                        Text(
                            text = "Выполнено",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
