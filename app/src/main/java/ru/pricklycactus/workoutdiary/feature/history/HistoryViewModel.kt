package ru.pricklycactus.workoutdiary.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository

class HistoryViewModel(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow(HistoryViewState())
    val viewState: StateFlow<HistoryViewState> = _viewState.asStateFlow()

    init {
        processEvent(HistoryUserEvent.LoadHistory)
    }

    fun processEvent(event: HistoryUserEvent) {
        when (event) {
            HistoryUserEvent.LoadHistory -> observeHistory()
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            repository.getWorkoutsWithExercises().collect { workouts ->
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

                _viewState.update { it.copy(workouts = grouped) }
            }
        }
    }
}
