package ru.pricklycactus.workoutdiary.feature.workout.impl

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
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.collectLatest
import ru.pricklycactus.workoutdiary.feature.common.Dimensions
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutEffect
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutExerciseState
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutExerciseStatus
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutIntent
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutStore
import ru.pricklycactus.workoutdiary.feature.workout.api.WorkoutViewState
import java.util.Locale

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
            title = { Text(stringResource(R.string.workout_finish_dialog_title)) },
            text = { Text(stringResource(R.string.workout_finish_dialog_text)) },
            confirmButton = {
                TextButton(onClick = { store.dispatch(WorkoutIntent.ConfirmFinish) }) {
                    Text(stringResource(R.string.workout_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { store.dispatch(WorkoutIntent.DismissFinishDialog) }) {
                    Text(stringResource(R.string.workout_no))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.ColumnSpacing)
    ) {
        Text(stringResource(R.string.workout_title), style = MaterialTheme.typography.headlineMedium)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimensions.ItemSpacing)
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
            Text(
                if (state.isSaving) {
                    stringResource(
                        R.string.workout_saving
                    )
                } else {
                    stringResource(R.string.workout_finish_button)
                }
            )
        }
    }
}

@Composable
private fun ExerciseItem(
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
            modifier = Modifier.padding(Dimensions.CardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.CardContentSpacing)
        ) {
            Text(
                text = exerciseState.exercise.name,
                style = MaterialTheme.typography.titleLarge
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.workout_sets_label), style = MaterialTheme.typography.bodyMedium)
                IconButton(
                    onClick = onDecreaseSets,
                    enabled = exerciseState.status != WorkoutExerciseStatus.COMPLETED
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = stringResource(R.string.workout_less_content_description)
                    )
                }
                Text(exerciseState.sets.toString(), style = MaterialTheme.typography.bodyLarge)
                IconButton(
                    onClick = onIncreaseSets,
                    enabled = exerciseState.status != WorkoutExerciseStatus.COMPLETED
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.workout_more_content_description)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.workout_reps_label), style = MaterialTheme.typography.bodyMedium)
                IconButton(
                    onClick = onDecreaseReps,
                    enabled = exerciseState.status != WorkoutExerciseStatus.COMPLETED
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = stringResource(R.string.workout_less_content_description)
                    )
                }
                Text(exerciseState.reps.toString(), style = MaterialTheme.typography.bodyLarge)
                IconButton(
                    onClick = onIncreaseReps,
                    enabled = exerciseState.status != WorkoutExerciseStatus.COMPLETED
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.workout_more_content_description)
                    )
                }
            }

            val durationMillis = exerciseState.durationMillis
            if (exerciseState.status == WorkoutExerciseStatus.COMPLETED && durationMillis != null) {
                Text(
                    text = stringResource(R.string.workout_duration_label, formatDuration(durationMillis)),
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
                            Text(stringResource(R.string.workout_start_button))
                        }
                    }
                    WorkoutExerciseStatus.IN_PROGRESS -> {
                        Button(onClick = onCompleteClick) {
                            Text(stringResource(R.string.workout_complete_button))
                        }
                    }
                    WorkoutExerciseStatus.COMPLETED -> {
                        Text(
                            text = stringResource(R.string.workout_completed_status),
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
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
