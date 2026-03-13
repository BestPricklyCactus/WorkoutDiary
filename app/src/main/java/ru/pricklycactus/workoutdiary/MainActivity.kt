package ru.pricklycactus.workoutdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import ru.pricklycactus.workoutdiary.data.database.WorkoutDatabaseProvider
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository
import ru.pricklycactus.workoutdiary.feature.history.HistoryScreen
import ru.pricklycactus.workoutdiary.feature.history.HistoryUserEvent
import ru.pricklycactus.workoutdiary.feature.history.HistoryViewModel
import ru.pricklycactus.workoutdiary.feature.main.MainScreen
import ru.pricklycactus.workoutdiary.feature.main.MainUserEvent
import ru.pricklycactus.workoutdiary.feature.main.MainViewModel
import ru.pricklycactus.workoutdiary.feature.workout.WorkoutScreen
import ru.pricklycactus.workoutdiary.feature.workout.WorkoutUserEvent
import ru.pricklycactus.workoutdiary.feature.workout.WorkoutViewModel
import ru.pricklycactus.workoutdiary.ui.theme.WorkoutDiaryTheme

class MainActivity : ComponentActivity(){
    private lateinit var viewModel: MainViewModel
    private lateinit var workoutRepository: WorkoutRepository

    private enum class Screen {
        MAIN,
        WORKOUT,
        HISTORY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = MainViewModel(this)
        val database = WorkoutDatabaseProvider.getDatabase(this)
        workoutRepository = WorkoutRepository(
            exerciseDao = database.exerciseDao(),
            workoutDao = database.workoutDao()
        )

        setContent {
            var currentScreen by remember { mutableStateOf(Screen.MAIN) }
            var workoutViewModel by remember { mutableStateOf<WorkoutViewModel?>(null) }
            var historyViewModel by remember { mutableStateOf<HistoryViewModel?>(null) }
            val mainViewState = viewModel.viewState.collectAsState().value

            WorkoutDiaryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.MAIN -> MainScreen(
                            viewState = mainViewState,
                            onEvent = viewModel::processEvent,
                            onShowHistoryClick = {
                                historyViewModel = HistoryViewModel(workoutRepository)
                                historyViewModel?.processEvent(HistoryUserEvent.LoadHistory)
                                currentScreen = Screen.HISTORY
                            },
                            onStartWorkoutClick = {
                                val selectedExercises = mainViewState.exercises.filter {
                                    it.id in mainViewState.selectedExerciseIds
                                }

                                if (selectedExercises.isNotEmpty()) {
                                    viewModel.processEvent(MainUserEvent.OnClick("start_workout"))
                                    workoutViewModel = WorkoutViewModel(
                                        selectedExercises = selectedExercises,
                                        repository = workoutRepository
                                    )
                                    currentScreen = Screen.WORKOUT
                                }
                            }
                        )

                        Screen.WORKOUT -> {
                            val currentWorkoutViewModel = workoutViewModel

                            if (currentWorkoutViewModel != null) {
                                val workoutViewState = currentWorkoutViewModel.viewState.collectAsState().value

                                WorkoutScreen(
                                    viewState = workoutViewState,
                                    onExerciseClick = {
                                        currentWorkoutViewModel.processEvent(
                                            WorkoutUserEvent.ExerciseClicked(it)
                                        )
                                    },
                                    onIncreaseReps = {
                                        currentWorkoutViewModel.processEvent(
                                            WorkoutUserEvent.IncreaseReps(it)
                                        )
                                    },
                                    onDecreaseReps = {
                                        currentWorkoutViewModel.processEvent(
                                            WorkoutUserEvent.DecreaseReps(it)
                                        )
                                    },
                                    onIncreaseSets = {
                                        currentWorkoutViewModel.processEvent(
                                            WorkoutUserEvent.IncreaseSets(it)
                                        )
                                    },
                                    onDecreaseSets = {
                                        currentWorkoutViewModel.processEvent(
                                            WorkoutUserEvent.DecreaseSets(it)
                                        )
                                    },
                                    onStartNow = {
                                        currentWorkoutViewModel.processEvent(
                                            WorkoutUserEvent.StartNow(it)
                                        )
                                    },
                                    onCompleteExercise = {
                                        currentWorkoutViewModel.processEvent(
                                            WorkoutUserEvent.CompleteExercise(it)
                                        )
                                    },
                                    onDismissDialog = {
                                        currentWorkoutViewModel.processEvent(WorkoutUserEvent.DismissDialog)
                                    },
                                    onBackClick = {
                                        currentWorkoutViewModel.processEvent(WorkoutUserEvent.DismissDialog)
                                        currentScreen = Screen.MAIN
                                    },
                                    onFinishWorkoutClick = {
                                        currentWorkoutViewModel.finishWorkout {
                                            workoutViewModel = null
                                            viewModel.processEvent(MainUserEvent.OnClick("back_to_main"))
                                            currentScreen = Screen.MAIN
                                        }
                                    }
                                )
                            } else {
                                currentScreen = Screen.MAIN
                            }
                        }

                        Screen.HISTORY -> {
                            val currentHistoryViewModel = historyViewModel

                            if (currentHistoryViewModel != null) {
                                val historyViewState = currentHistoryViewModel.viewState.collectAsState().value

                                HistoryScreen(
                                    viewState = historyViewState,
                                    onBackClick = {
                                        currentScreen = Screen.MAIN
                                    }
                                )
                            } else {
                                currentScreen = Screen.MAIN
                            }
                        }
                    }
                }
            }
        }
    }
}
