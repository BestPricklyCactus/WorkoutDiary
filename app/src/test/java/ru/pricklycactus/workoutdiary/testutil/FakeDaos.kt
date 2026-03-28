package ru.pricklycactus.workoutdiary.testutil

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.pricklycactus.workoutdiary.data.dao.ExerciseDao
import ru.pricklycactus.workoutdiary.data.dao.WorkoutDao
import ru.pricklycactus.workoutdiary.data.database.Exercise
import ru.pricklycactus.workoutdiary.data.database.Workout
import ru.pricklycactus.workoutdiary.data.model.WorkoutWithExercise
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository

class FakeExerciseDao(
    initialExercises: List<Exercise> = emptyList()
) : ExerciseDao {
    private val state = MutableStateFlow(initialExercises)

    override fun getAllExercises(): Flow<List<Exercise>> = state

    override suspend fun upsertExercise(exercise: Exercise) {
        val current = state.value.toMutableList()
        val index = current.indexOfFirst { it.id == exercise.id && exercise.id != 0L }
        if (index >= 0) {
            current[index] = exercise
        } else {
            val nextId = if (exercise.id != 0L) exercise.id else (current.maxOfOrNull { it.id } ?: 0L) + 1
            current.add(exercise.copy(id = nextId))
        }
        state.value = current
    }

    override suspend fun deleteExercise(exercise: Exercise) {
        state.value = state.value.filterNot { it.id == exercise.id }
    }
}

class FakeWorkoutDao(
    initialWorkouts: List<WorkoutWithExercise> = emptyList()
) : WorkoutDao {
    private val relationsState = MutableStateFlow(initialWorkouts)
    private val workoutsState = MutableStateFlow(initialWorkouts.map { it.workout })

    override fun getAllWorkouts(): Flow<List<Workout>> = workoutsState

    override fun getWorkoutsWithExercises(): Flow<List<WorkoutWithExercise>> = relationsState

    override suspend fun getLastWorkoutForExercise(exerciseId: Long): Workout? =
        workoutsState.value.filter { it.exerciseId == exerciseId }.maxByOrNull { it.workoutDate }

    override suspend fun insertWorkout(workout: Workout) {
        workoutsState.value = workoutsState.value + workout
    }

    override suspend fun insertWorkouts(workouts: List<Workout>) {
        workoutsState.value = workoutsState.value + workouts
    }

    override suspend fun updateWorkout(workout: Workout) = Unit

    override suspend fun deleteWorkout(workout: Workout) = Unit
}

fun createWorkoutRepository(
    exercises: List<Exercise> = emptyList(),
    workouts: List<WorkoutWithExercise> = emptyList()
): WorkoutRepository = WorkoutRepository(
    exerciseDao = FakeExerciseDao(exercises),
    workoutDao = FakeWorkoutDao(workouts)
)
