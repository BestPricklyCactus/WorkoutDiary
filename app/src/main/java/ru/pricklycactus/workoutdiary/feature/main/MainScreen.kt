package ru.pricklycactus.workoutdiary.feature.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.collectLatest
import ru.pricklycactus.workoutdiary.R
import ru.pricklycactus.workoutdiary.ui.theme.Dimensions

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
            .padding(Dimensions.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.ColumnSpacing)
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
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            if (state.selectedExerciseIds.isNotEmpty()) {
                Button(
                    onClick = { store.dispatch(MainIntent.NavigateToWorkout) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.main_start_workout))
                }
            }
        }
    }
}
