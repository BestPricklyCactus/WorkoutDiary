package ru.pricklycactus.workoutdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository
import ru.pricklycactus.workoutdiary.feature.editor.EditorScreen
import ru.pricklycactus.workoutdiary.feature.editor.EditorViewModel
import ru.pricklycactus.workoutdiary.feature.history.HistoryScreen
import ru.pricklycactus.workoutdiary.feature.history.HistoryViewModel
import ru.pricklycactus.workoutdiary.feature.main.MainScreen
import ru.pricklycactus.workoutdiary.feature.main.MainViewModel
import ru.pricklycactus.workoutdiary.feature.workout.WorkoutScreen
import ru.pricklycactus.workoutdiary.feature.workout.WorkoutViewModel
import ru.pricklycactus.workoutdiary.ui.theme.WorkoutDiaryTheme
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var workoutRepository: WorkoutRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WorkoutDiaryApplication).appComponent.inject(this)

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val mainViewModel = remember { MainViewModel(this, workoutRepository) }
            val mainViewState = mainViewModel.viewState.collectAsState().value

            WorkoutDiaryTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(screen.label) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Main.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Main.route) {
                            MainScreen(
                                viewState = mainViewState,
                                onEvent = mainViewModel::processEvent,
                                onNavigateToWorkout = { navController.navigate("workout_process") }
                            )
                        }
                        composable("workout_process") {
                            // Logic to get selected exercises from mainViewState or elsewhere
                            val selectedExercises = mainViewState.exercises.filter { it.id in mainViewState.selectedExerciseIds }
                            val workoutViewModel = remember { WorkoutViewModel(selectedExercises, workoutRepository) }
                            val workoutViewState = workoutViewModel.viewState.collectAsState().value

                            // Установка колбэка для навигации назад после сохранения
                            workoutViewModel.setOnFinishedCallback {
                                navController.popBackStack()
                            }

                            WorkoutScreen(
                                viewState = workoutViewState,
                                onEvent = workoutViewModel::processEvent,
                            )
                        }
                        composable(Screen.History.route) {
                            val historyViewModel = remember { HistoryViewModel(workoutRepository) }
                            val historyViewState = historyViewModel.viewState.collectAsState().value

                            HistoryScreen(
                                viewState = historyViewState,
                                onEvent = historyViewModel::processEvent
                            )
                        }
                        composable(Screen.Editor.route) {
                            val editorViewModel = remember { EditorViewModel(workoutRepository) }
                            val editorViewState = editorViewModel.viewState.collectAsState().value

                            EditorScreen(
                                viewState = editorViewState,
                                onEvent = editorViewModel::processEvent
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Main : Screen("main", "Тренировка", Icons.Filled.PlayArrow)
    object History : Screen("history", "История", Icons.Filled.History)
    object Editor : Screen("editor", "Редактор", Icons.Filled.Settings)
}

val items = listOf(
    Screen.Main,
    Screen.History,
    Screen.Editor
)
