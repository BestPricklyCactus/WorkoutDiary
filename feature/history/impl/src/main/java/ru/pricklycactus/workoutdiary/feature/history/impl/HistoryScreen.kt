package ru.pricklycactus.workoutdiary.feature.history.impl

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.pricklycactus.workoutdiary.feature.common.Dimensions
import ru.pricklycactus.workoutdiary.feature.history.api.HistoryIntent
import ru.pricklycactus.workoutdiary.feature.history.api.HistoryStore
import ru.pricklycactus.workoutdiary.feature.history.api.HistoryViewState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    state: HistoryViewState,
    store: HistoryStore,
    onNavigateToReport: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimensions.ColumnSpacing)
    ) {
        Text(
            text = stringResource(R.string.history_title),
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = onNavigateToReport,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.history_show_report))
        }

        if (state.workouts.isNotEmpty()) {
            Text(
                text = stringResource(R.string.history_delete_hint),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (state.workouts.isEmpty()) {
            Text(
                text = stringResource(R.string.history_empty),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.ItemSpacing)
            ) {
                items(state.workouts, key = { it.workoutDate }) { workout ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {},
                                onLongClick = {
                                    store.dispatch(HistoryIntent.RequestWorkoutDeletion(workout.workoutDate))
                                }
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimensions.CardPadding),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.CardContentSpacing)
                        ) {
                            Text(
                                text = formatWorkoutDate(workout.workoutDate),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(
                                    R.string.history_total_time,
                                    formatDuration(workout.totalDurationMillis)
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            workout.exercises.forEach { exercise ->
                                Text(
                                    text = stringResource(
                                        R.string.history_exercise_format,
                                        exercise.exerciseName,
                                        exercise.sets,
                                        exercise.reps
                                    ),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.workoutDatePendingDeletion != null) {
            AlertDialog(
                onDismissRequest = { store.dispatch(HistoryIntent.DismissWorkoutDeletion) },
                title = { Text(stringResource(R.string.history_delete_dialog_title)) },
                text = { Text(stringResource(R.string.history_delete_dialog_message)) },
                confirmButton = {
                    TextButton(onClick = { store.dispatch(HistoryIntent.ConfirmWorkoutDeletion) }) {
                        Text(stringResource(R.string.history_delete_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { store.dispatch(HistoryIntent.DismissWorkoutDeletion) }) {
                        Text(stringResource(R.string.history_delete_cancel))
                    }
                }
            )
        }
    }
}

private fun formatWorkoutDate(workoutDate: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(workoutDate))
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
