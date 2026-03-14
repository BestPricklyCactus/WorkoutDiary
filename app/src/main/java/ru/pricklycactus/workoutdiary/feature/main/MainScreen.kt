package ru.pricklycactus.workoutdiary.feature.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    viewState: MainViewState,
    onEvent: (MainUserEvent) -> Unit,
    onNavigateToWorkout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!viewState.showExercisesList) {
            Button(
                onClick = { onEvent(MainUserEvent.OnClick("show_exercises")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("К списку упражнений")
            }
        } else {
            ExercisesList(
                exercises = viewState.exercises,
                selectedExerciseIds = viewState.selectedExerciseIds,
                onExerciseSelect = { id, selected ->
                    onEvent(MainUserEvent.OnExerciseSelected(id, selected))
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            if (viewState.selectedExerciseIds.isNotEmpty()) {
                Button(
                    onClick = onNavigateToWorkout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Начать тренировку")
                }
            }
        }
    }
}
