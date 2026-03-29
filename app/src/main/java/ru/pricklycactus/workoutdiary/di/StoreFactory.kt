package ru.pricklycactus.workoutdiary.di

import kotlinx.coroutines.CoroutineScope
import ru.pricklycactus.workoutdiary.data.domain.ExerciseDomain
import ru.pricklycactus.workoutdiary.data.repository.WorkoutRepository
import ru.pricklycactus.workoutdiary.feature.aiworkout.impl.AiWorkoutStoreImpl
import ru.pricklycactus.workoutdiary.feature.aiworkout.impl.LlmWorkoutGenerator
import ru.pricklycactus.workoutdiary.feature.editor.impl.EditorStoreImpl
import ru.pricklycactus.workoutdiary.feature.history.impl.HistoryStoreImpl
import ru.pricklycactus.workoutdiary.feature.main.impl.MainStoreImpl
import ru.pricklycactus.workoutdiary.feature.report.impl.ReportStoreImpl
import ru.pricklycactus.workoutdiary.feature.workout.impl.WorkoutStoreImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreFactory @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val llmWorkoutGenerator: LlmWorkoutGenerator
) {

    fun createMainStore(scope: CoroutineScope) = MainStoreImpl(workoutRepository, scope)

    fun createEditorStore(scope: CoroutineScope) = EditorStoreImpl(workoutRepository, scope)

    fun createHistoryStore(scope: CoroutineScope) = HistoryStoreImpl(workoutRepository, scope)

    fun createReportStore(scope: CoroutineScope) = ReportStoreImpl(workoutRepository, scope)

    fun createWorkoutStore(
        selectedExercises: List<ExerciseDomain>,
        scope: CoroutineScope
    ) = WorkoutStoreImpl(selectedExercises, workoutRepository, scope)

    fun createAiWorkoutStore(scope: CoroutineScope) =
        AiWorkoutStoreImpl(
            repository = workoutRepository,
            generator = llmWorkoutGenerator,
            scope = scope
        )
}
