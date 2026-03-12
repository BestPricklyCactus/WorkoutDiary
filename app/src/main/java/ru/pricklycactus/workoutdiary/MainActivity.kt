package ru.pricklycactus.workoutdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ru.pricklycactus.workoutdiary.ui.MainScreen
import ru.pricklycactus.workoutdiary.ui.MainUserEvent
import ru.pricklycactus.workoutdiary.ui.theme.WorkoutDiaryTheme
import androidx.compose.runtime.collectAsState
import ru.pricklycactus.workoutdiary.ui.MainViewModel

class MainActivity : ComponentActivity(){
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = MainViewModel(this)

        setContent {
            WorkoutDiaryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        viewState = viewModel.viewState.collectAsState().value,
                        onAddExerciseClick = {
                            viewModel.processEvent(MainUserEvent.OnClick("add_exercise"))
                        },
                        onShowExercisesClick = {
                            viewModel.processEvent(MainUserEvent.OnClick("show_exercises"))
                        },
                        onSaveExerciseClick = {
                            viewModel.processEvent(MainUserEvent.OnClick("save_exercise"))
                        },
                        onCancelAddExerciseClick = {
                            viewModel.processEvent(MainUserEvent.OnClick("cancel_add_exercise"))
                        },
                        onExerciseNameChange = { text ->
                            viewModel.processEvent(MainUserEvent.OnTextChanged("name", text))
                        },
                        onExerciseDescriptionChange = { text ->
                            viewModel.processEvent(MainUserEvent.OnTextChanged("description", text))
                        },
                        onExerciseSelected = { id, selected ->
                            viewModel.processEvent(MainUserEvent.OnExerciseSelected(id, selected))
                        },
                        onStartWorkoutClick = {
                            viewModel.processEvent(MainUserEvent.OnClick("start_workout"))
                        },
                        onBackToHome = {
                            viewModel.processEvent(MainUserEvent.OnClick("back_to_main"))
                        }
                    )
                }
            }
        }
    }
}