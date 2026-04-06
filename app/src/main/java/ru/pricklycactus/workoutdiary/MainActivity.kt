package ru.pricklycactus.workoutdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.pricklycactus.workoutdiary.feature.aiworkout.impl.AiWorkoutScreen
import ru.pricklycactus.workoutdiary.feature.editor.impl.EditorScreen
import ru.pricklycactus.workoutdiary.feature.history.impl.HistoryScreen
import ru.pricklycactus.workoutdiary.feature.history.impl.HistoryWorkoutDetailScreen
import ru.pricklycactus.workoutdiary.feature.main.impl.MainScreen
import ru.pricklycactus.workoutdiary.feature.report.impl.ReportScreen
import ru.pricklycactus.workoutdiary.feature.workout.impl.WorkoutScreen
import ru.pricklycactus.workoutdiary.di.StoreFactory
import ru.pricklycactus.workoutdiary.di.StoresViewModel
import ru.pricklycactus.workoutdiary.ui.theme.WorkoutDiaryTheme
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var storeFactory: StoreFactory

    private val storesViewModel: StoresViewModel by viewModels {
        StoresViewModel.Factory(storeFactory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as WorkoutDiaryApplication).appComponent.inject(this)

        setContent {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val mainStore = storesViewModel.mainStore
            val mainState by mainStore.state.collectAsState()
            val historyStore = storesViewModel.historyStore
            val historyState by historyStore.state.collectAsState()

            WorkoutDiaryTheme {
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(stringResource(screen.labelResId)) },
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
                                state = mainState,
                                store = mainStore,
                                onNavigateToWorkout = { navController.navigate(ScreenRoutes.WorkoutProcess) }
                            )
                        }
                        composable(ScreenRoutes.WorkoutProcess) {
                            val selectedExercises = mainState.exercises.filter { it.id in mainState.selectedExerciseIds }
                            val workoutStore = storesViewModel.getOrCreateWorkoutStore(selectedExercises)
                            val workoutState by workoutStore.state.collectAsState()

                            WorkoutScreen(
                                state = workoutState,
                                store = workoutStore,
                                onBack = {
                                    storesViewModel.clearWorkoutStore()
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(Screen.History.route) {
                            HistoryScreen(
                                state = historyState,
                                store = historyStore,
                                onNavigateToReport = { navController.navigate(ScreenRoutes.Report) },
                                onWorkoutClick = { workoutDate ->
                                    navController.navigate("${ScreenRoutes.HistoryDetail}/$workoutDate")
                                }
                            )
                        }
                        composable("${ScreenRoutes.HistoryDetail}/{workoutDate}") { backStackEntry ->
                            val workoutDate = backStackEntry.arguments?.getString("workoutDate")?.toLongOrNull()
                            val workout = historyState.workouts.firstOrNull { it.workoutDate == workoutDate }

                            if (workout != null) {
                                HistoryWorkoutDetailScreen(
                                    workout = workout,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(ScreenRoutes.Report) {
                            val reportStore = storesViewModel.reportStore
                            val reportState by reportStore.state.collectAsState()

                            ReportScreen(
                                state = reportState,
                                store = reportStore,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.Editor.route) {
                            val editorStore = storesViewModel.editorStore
                            val editorState by editorStore.state.collectAsState()

                            EditorScreen(
                                state = editorState,
                                store = editorStore
                            )
                        }
                        composable(Screen.AiWorkout.route) {
                            val aiWorkoutStore = storesViewModel.aiWorkoutStore
                            val aiWorkoutState by aiWorkoutStore.state.collectAsState()

                            AiWorkoutScreen(
                                state = aiWorkoutState,
                                store = aiWorkoutStore
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed class Screen(val route: String, val labelResId: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Main : Screen(ScreenRoutes.Main, R.string.nav_label_workout, Icons.Filled.PlayArrow)
    object History : Screen(ScreenRoutes.History, R.string.nav_label_history, Icons.Filled.History)
    object Editor : Screen(ScreenRoutes.Editor, R.string.nav_label_editor, Icons.Filled.Settings)
    object AiWorkout : Screen(ScreenRoutes.AiWorkout, R.string.nav_label_ai_workout, Icons.Filled.AutoAwesome)
}

object ScreenRoutes {
    const val Main = "main"
    const val History = "history"
    const val Editor = "editor"
    const val AiWorkout = "ai_workout"
    const val WorkoutProcess = "workout_process"
    const val Report = "report"
    const val HistoryDetail = "history_detail"
}

val items = listOf(
    Screen.Main,
    Screen.History,
    Screen.Editor,
    Screen.AiWorkout
)
