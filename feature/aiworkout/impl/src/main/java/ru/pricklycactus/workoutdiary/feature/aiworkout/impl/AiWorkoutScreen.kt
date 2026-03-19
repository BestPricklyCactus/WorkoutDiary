package ru.pricklycactus.workoutdiary.feature.aiworkout.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.pricklycactus.workoutdiary.ui.theme.Dimensions
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiGeneratedExercise
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiWorkoutIntent
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiWorkoutStore
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiWorkoutViewState

@Composable
fun AiWorkoutScreen(
    state: AiWorkoutViewState,
    store: AiWorkoutStore
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimensions.ColumnSpacing)
    ) {
        Text(
            text = stringResource(R.string.ai_workout_title),
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = state.prompt,
            onValueChange = { store.dispatch(AiWorkoutIntent.OnPromptChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.ai_workout_prompt_label)) },
            placeholder = { Text(stringResource(R.string.ai_workout_prompt_placeholder)) }
        )

        Button(
            onClick = { store.dispatch(AiWorkoutIntent.GenerateWorkoutClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(end = Dimensions.ItemSpacing))
            }
            Text(stringResource(R.string.ai_workout_generate))
        }

        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        if (state.generatedTitle.isNotBlank()) {
            Text(text = state.generatedTitle, style = MaterialTheme.typography.titleLarge)
        }

        if (state.generatedExercises.isNotEmpty()) {
            OutlinedButton(
                onClick = { store.dispatch(AiWorkoutIntent.SaveAllExercisesToDatabase) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.ai_workout_save_all))
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimensions.ListSpacing)
            ) {
                items(state.generatedExercises, key = { it.id }) { exercise ->
                    AiExerciseCard(exercise = exercise, store = store)
                }
            }
        }
    }
}

@Composable
private fun AiExerciseCard(
    exercise: AiGeneratedExercise,
    store: AiWorkoutStore
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(Dimensions.CardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimensions.CardContentSpacing)
        ) {
            Text(text = exercise.name, style = MaterialTheme.typography.titleMedium)
            Text(text = exercise.description, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(
                    R.string.ai_workout_sets_reps_format,
                    exercise.sets,
                    exercise.reps
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = { store.dispatch(AiWorkoutIntent.AddExerciseToDatabase(exercise.id)) },
                    enabled = !exercise.isSaved
                ) {
                    Text(
                        if (exercise.isSaved) {
                            stringResource(R.string.ai_workout_saved)
                        } else {
                            stringResource(R.string.ai_workout_add_to_db)
                        }
                    )
                }
            }
        }
    }
}
