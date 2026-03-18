package ru.pricklycactus.workoutdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository
import ru.pricklycactus.workoutdiary.feature.aiworkout.impl.AiWorkoutScreen
import ru.pricklycactus.workoutdiary.feature.aiworkout.impl.BuildConfig as AiWorkoutBuildConfig
import ru.pricklycactus.workoutdiary.feature.aiworkout.impl.AiWorkoutStoreImpl
import ru.pricklycactus.workoutdiary.feature.aiworkout.impl.LlmWorkoutGenerator
import ru.pricklycactus.workoutdiary.feature.editor.impl.EditorScreen
import ru.pricklycactus.workoutdiary.feature.editor.impl.EditorStoreImpl
import ru.pricklycactus.workoutdiary.feature.history.impl.HistoryScreen
import ru.pricklycactus.workoutdiary.feature.history.impl.HistoryStoreImpl
import ru.pricklycactus.workoutdiary.feature.main.impl.MainScreen
import ru.pricklycactus.workoutdiary.feature.main.impl.MainStoreImpl
import ru.pricklycactus.workoutdiary.feature.workout.impl.WorkoutScreen
import ru.pricklycactus.workoutdiary.feature.workout.impl.WorkoutStoreImpl
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
            val scope = rememberCoroutineScope()

            val mainStore = remember { MainStoreImpl(workoutRepository, scope) }
            val mainState by mainStore.state.collectAsState()

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
                                onNavigateToWorkout = { navController.navigate("workout_process") }
                            )
                        }
                        composable("workout_process") {
                            val selectedExercises = mainState.exercises.filter { it.id in mainState.selectedExerciseIds }
                            val workoutStore = remember { WorkoutStoreImpl(selectedExercises, workoutRepository, scope) }
                            val workoutState by workoutStore.state.collectAsState()

                            WorkoutScreen(
                                state = workoutState,
                                store = workoutStore,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Screen.History.route) {
                            val historyStore = remember { HistoryStoreImpl(workoutRepository, scope) }
                            val historyState by historyStore.state.collectAsState()

                            HistoryScreen(
                                state = historyState,
                                store = historyStore
                            )
                        }
                        composable(Screen.Editor.route) {
                            val editorStore = remember { EditorStoreImpl(workoutRepository, scope) }
                            val editorState by editorStore.state.collectAsState()

                            EditorScreen(
                                state = editorState,
                                store = editorStore
                            )
                        }
                        composable(Screen.AiWorkout.route) {
                            val aiWorkoutStore = remember {
                                AiWorkoutStoreImpl(
                                    repository = workoutRepository,
                                    generator = LlmWorkoutGenerator(
                                        apiKey = AiWorkoutBuildConfig.LLM_API_KEY,
                                        model = AiWorkoutBuildConfig.LLM_MODEL
                                    ),
                                    scope = scope
                                )
                            }
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
    object Main : Screen("main", R.string.nav_label_workout, Icons.Filled.PlayArrow)
    object History : Screen("history", R.string.nav_label_history, Icons.Filled.History)
    object Editor : Screen("editor", R.string.nav_label_editor, Icons.Filled.Settings)
    object AiWorkout : Screen("ai_workout", R.string.nav_label_ai_workout, Icons.Filled.AutoAwesome)
}

val items = listOf(
    Screen.Main,
    Screen.History,
    Screen.Editor,
    Screen.AiWorkout
)
