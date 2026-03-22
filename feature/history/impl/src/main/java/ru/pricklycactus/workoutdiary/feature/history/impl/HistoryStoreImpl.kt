package ru.pricklycactus.workoutdiary.feature.history.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.core.mvi.MviStore
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository
import ru.pricklycactus.workoutdiary.feature.history.api.HistoryEffect
import ru.pricklycactus.workoutdiary.feature.history.api.HistoryIntent
import ru.pricklycactus.workoutdiary.feature.history.api.HistoryStore
import ru.pricklycactus.workoutdiary.feature.history.api.HistoryViewState
import ru.pricklycactus.workoutdiary.feature.history.api.WorkoutHistoryEntry
import ru.pricklycactus.workoutdiary.feature.history.api.WorkoutHistoryItem

class HistoryStoreImpl(
    private val repository: WorkoutRepository,
    scope: CoroutineScope
) : MviStore<HistoryViewState, HistoryIntent, HistoryEffect>(HistoryViewState(), scope), HistoryStore {

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
                    .groupBy { it.workout.date }
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
