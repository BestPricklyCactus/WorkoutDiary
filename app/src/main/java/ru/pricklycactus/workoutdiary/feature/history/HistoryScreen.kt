package ru.pricklycactus.workoutdiary.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.pricklycactus.workoutdiary.ui.theme.Dimensions
import ru.pricklycactus.workoutdiary.ui.theme.TimeConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewState: HistoryViewState,
    onEvent: (HistoryUserEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimensions.ColumnSpacing)
    ) {
        Text("История тренировок", style = MaterialTheme.typography.headlineMedium)

        if (viewState.workouts.isEmpty()) {
            Text("Тренировок пока нет", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.ListSpacing)
            ) {
                items(viewState.workouts, key = { it.workoutDate }) { workout ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(Dimensions.CardPadding),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.CardContentSpacing)
                        ) {
                            Text(
                                text = formatWorkoutDate(workout.workoutDate),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Общее время: ${formatDuration(workout.totalDurationMillis)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            workout.exercises.forEach { exercise ->
                                Text(
                                    text = "${exercise.exerciseName}: ${exercise.sets} подходов x ${exercise.reps} повторений",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun formatWorkoutDate(workoutDate: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(workoutDate))
}

private fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / TimeConstants.MillisPerSecond
    val minutes = totalSeconds / TimeConstants.SecondsPerMinute
    val seconds = totalSeconds % TimeConstants.SecondsPerMinute
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
