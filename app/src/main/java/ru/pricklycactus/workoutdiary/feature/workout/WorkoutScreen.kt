package ru.pricklycactus.workoutdiary.feature.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit

@Composable
fun WorkoutScreen(
    viewState: WorkoutViewState,
    onExerciseClick: (Long) -> Unit,
    onIncreaseReps: (Long) -> Unit,
    onDecreaseReps: (Long) -> Unit,
    onIncreaseSets: (Long) -> Unit,
    onDecreaseSets: (Long) -> Unit,
    onStartNow: (Long) -> Unit,
    onCompleteExercise: (Long) -> Unit,
    onDismissDialog: () -> Unit,
    onBackClick: () -> Unit,
    onFinishWorkoutClick: () -> Unit
) {
    val selectedExercise = viewState.exercises.firstOrNull {
        it.exercise.id == viewState.selectedExerciseId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Текущая тренировка", style = MaterialTheme.typography.headlineMedium)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewState.exercises, key = { it.exercise.id }) { exerciseState ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onExerciseClick(exerciseState.exercise.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = when (exerciseState.status) {
                            WorkoutExerciseStatus.IDLE -> MaterialTheme.colorScheme.surfaceVariant
                            WorkoutExerciseStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
                            WorkoutExerciseStatus.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(exerciseState.exercise.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            exerciseState.exercise.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Статус: ${exerciseState.status.toLabel()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (exerciseState.durationMillis != null) {
                            Text(
                                text = "Время выполнения: ${exerciseState.durationMillis.formatDuration()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CounterControl(
                                label = "Повторения",
                                value = exerciseState.reps,
                                onDecrease = { onDecreaseReps(exerciseState.exercise.id) },
                                onIncrease = { onIncreaseReps(exerciseState.exercise.id) },
                                modifier = Modifier.weight(1f)
                            )
                            CounterControl(
                                label = "Подходы",
                                value = exerciseState.sets,
                                onDecrease = { onDecreaseSets(exerciseState.exercise.id) },
                                onIncrease = { onIncreaseSets(exerciseState.exercise.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Назад")
        }

        Button(
            onClick = onFinishWorkoutClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewState.isSaving && viewState.exercises.isNotEmpty()
        ) {
            if (viewState.isSaving) {
                CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text("Завершить тренировку")
        }
    }

    if (selectedExercise != null) {
        AlertDialog(
            onDismissRequest = onDismissDialog,
            title = { Text(selectedExercise.exercise.name) },
            text = { Text("Выберите действие для упражнения") },
            confirmButton = {
                Button(onClick = { onStartNow(selectedExercise.exercise.id) }) {
                    Text("Начать сейчас")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onCompleteExercise(selectedExercise.exercise.id) }) {
                        Text("Завершить")
                    }
                    TextButton(onClick = onDismissDialog) {
                        Text("Отмена")
                    }
                }
            }
        )
    }
}

private fun WorkoutExerciseStatus.toLabel(): String = when (this) {
    WorkoutExerciseStatus.IDLE -> "Не начато"
    WorkoutExerciseStatus.IN_PROGRESS -> "В процессе"
    WorkoutExerciseStatus.COMPLETED -> "Завершено"
}

@Composable
private fun CounterControl(
    label: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onDecrease) {
                Text("-")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = value.toString(), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onIncrease) {
                Text("+")
            }
        }
    }
}

private fun Long.formatDuration(): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(this)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
