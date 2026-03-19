package ru.pricklycactus.workoutdiary.feature.main.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.collectLatest
import ru.pricklycactus.workoutdiary.feature.common.ExercisesList
import ru.pricklycactus.workoutdiary.feature.main.api.MainEffect
import ru.pricklycactus.workoutdiary.feature.main.api.MainIntent
import ru.pricklycactus.workoutdiary.feature.main.api.MainStore
import ru.pricklycactus.workoutdiary.feature.main.api.MainViewState
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
                onClick = { store.dispatch(MainIntent.OnClick(MainActions.ShowExercises)) },
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

internal object MainActions {
    const val ShowExercises = "show_exercises"
}
