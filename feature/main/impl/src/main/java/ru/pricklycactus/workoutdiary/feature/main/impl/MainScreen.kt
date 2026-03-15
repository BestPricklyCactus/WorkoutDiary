package ru.pricklycactus.workoutdiary.feature.main.impl

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import ru.pricklycactus.workoutdiary.feature.common.ExercisesList
import ru.pricklycactus.workoutdiary.feature.main.api.MainEffect
import ru.pricklycactus.workoutdiary.feature.main.api.MainIntent
import ru.pricklycactus.workoutdiary.feature.main.api.MainStore
import ru.pricklycactus.workoutdiary.feature.main.api.MainViewState

@Composable
fun MainScreen(
    state: MainViewState,
    store: MainStore,
    onNavigateToWorkout: () -> Unit
) {
    LaunchedEffect(Unit) {
        store.effect.collectLatest { effect ->
            when (effect) {
                MainEffect.NavigateToWorkout -> onNavigateToWorkout()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!state.showExercisesList) {
            Button(
                onClick = { store.dispatch(MainIntent.OnClick("show_exercises")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.main_to_exercises_list))
            }
        } else {
            ExercisesList(
                exercises = state.exercises,
                selectedExerciseIds = state.selectedExerciseIds,
                onExerciseSelect = { id, selected ->
                    store.dispatch(MainIntent.OnExerciseSelected(id, selected))
                },
                modifier = Modifier.weight(1f)
            )

            if (state.selectedExerciseIds.isNotEmpty()) {
                ElevatedButton(
                    onClick = { store.dispatch(MainIntent.NavigateToWorkout) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.main_start_workout))
                }
            }
        }
    }
}
