package ru.pricklycactus.workoutdiary.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen(
    viewState: MainViewState,
    onAddExerciseClick: () -> Unit,
    onShowExercisesClick: () -> Unit,
    onSaveExerciseClick: () -> Unit,
    onCancelAddExerciseClick: () -> Unit,
    onExerciseNameChange: (String) -> Unit,
    onExerciseDescriptionChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (viewState.showAddExerciseForm) {
            // Форма добавления упражнения
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = viewState.exerciseName,
                    onValueChange = onExerciseNameChange,
                    label = { Text("Название упражнения") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                OutlinedTextField(
                    value = viewState.exerciseDescription,
                    onValueChange = onExerciseDescriptionChange,
                    label = { Text("Описание") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Button(onClick = onSaveExerciseClick) {
                    Text("Добавить")
                }
                Button(onClick = onCancelAddExerciseClick) {
                    Text("Отмена")
                }
            }
        } else {
            // Основной экран
            Button(onClick = onAddExerciseClick) {
                Text("Добавить упражнение")
            }
            Button(onClick = onShowExercisesClick) {
                Text("Просмотреть упражнения")
            }
        }
    }
}



// События от пользователя
sealed class MainUserEvent {
    data class OnClick(val action: String) : MainUserEvent()
    data class OnTextChanged(val field: String, val text: String) : MainUserEvent()
}