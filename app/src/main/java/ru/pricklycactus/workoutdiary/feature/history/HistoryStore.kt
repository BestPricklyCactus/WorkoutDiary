package ru.pricklycactus.workoutdiary.feature.history

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.core.mvi.MviStore
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository

class HistoryStore(
    private val repository: WorkoutRepository,
    scope: CoroutineScope
) : MviStore<HistoryViewState, HistoryIntent, HistoryEffect>(HistoryViewState(), scope) {

    init {
        dispatch(HistoryIntent.LoadHistory)
    }

    override fun dispatch(intent: HistoryIntent) {
        when (intent) {
            HistoryIntent.LoadHistory -> observeHistory()
        }
    }

    private fun observeHistory() {
        scope.launch {
            repository.getWorkoutsWithExercises().collectLatest { workouts ->
                val grouped = workouts
                    .groupBy { it.workout.workoutDate }
                    .toSortedMap(compareByDescending { it })
                    .map { (workoutDate, entries) ->
                        WorkoutHistoryEntry(
                            workoutDate = workoutDate,
                            totalDurationMillis = entries.firstOrNull()?.workout?.totalDurationMillis ?: 0L,
                            exercises = entries.map { entry ->
                                WorkoutHistoryItem(
                                    exerciseName = entry.exercise.name,
                                    sets = entry.workout.sets,
                                    reps = entry.workout.reps
                                )
                            }
                        )
                    }

                updateState { it.copy(workouts = grouped) }
            }
        }
    }
}
