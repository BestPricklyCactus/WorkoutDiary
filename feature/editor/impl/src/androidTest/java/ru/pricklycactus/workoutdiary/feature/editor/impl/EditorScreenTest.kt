package ru.pricklycactus.workoutdiary.feature.editor.impl

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import ru.pricklycactus.workoutdiary.data.domain.ExerciseDomain
import ru.pricklycactus.workoutdiary.feature.editor.api.EditorIntent
import ru.pricklycactus.workoutdiary.feature.editor.api.EditorStore
import ru.pricklycactus.workoutdiary.feature.editor.api.EditorViewState

class EditorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockStore = mockk<EditorStore>(relaxed = true)

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Use mutableState to allow updating the view state during the test
    private val testState = mutableStateOf(EditorViewState())

    @Test
    fun addExerciseScenario_Success() {
        // 1. Initial state: Form is hidden
        testState.value = EditorViewState(showAddExerciseForm = false)
        setupScreen()

        // 2. Нажимаем "Добавить упражнение"
        val addText = context.getString(R.string.editor_add_exercise)
        composeTestRule.onNodeWithText(addText).performClick()
        
        verify { mockStore.dispatch(EditorIntent.AddExerciseClick) }

        // 3. Обновление состояния: Форма отображается (имитируем процесс обновления состояния в хранилище).
        testState.value = EditorViewState(showAddExerciseForm = true)

        // 4. Заполняем форму
        val nameLabel = context.getString(ru.pricklycactus.workoutdiary.feature.common.R.string.exercise_name_label)
        val saveText = context.getString(ru.pricklycactus.workoutdiary.feature.common.R.string.exercise_save)

        composeTestRule.onNodeWithText(nameLabel).assertIsDisplayed()
        
        val testName = "Отжимания"
        composeTestRule.onNodeWithText(nameLabel).performTextInput(testName)
        

        // текст вводится через интент
        verify { 
            mockStore.dispatch(match { 
                it is EditorIntent.OnTextChanged && it.text == testName 
            }) 
        }

        // 5. Сохраняем
        composeTestRule.onNodeWithText(saveText).performClick()
        verify { mockStore.dispatch(EditorIntent.SaveExerciseClick) }
    }

    @Test
    fun deleteExerciseScenario_Success() {
        // 1. Arrange: Состояние со списком упражнений
        val exerciseId = 1L
        val exerciseName = "Подтягивания"
        val exercises = listOf(
            ExerciseDomain(
                id = exerciseId,
                name = exerciseName,
                description = ""
            )
        )

        testState.value = EditorViewState(exercises = exercises)
        setupScreen()

        // 2. Act: Выбираем упражнение в списке
        composeTestRule.onNodeWithText(exerciseName).performClick()

        // Assert: Проверяем, что ушел Intent на выбор
        verify { mockStore.dispatch(EditorIntent.OnExerciseSelected(exerciseId, true)) }

        // 3. Arrange: Имитируем обновление состояния (упражнение теперь выбрано)
        testState.value = testState.value.copy(selectedExerciseIds = setOf(exerciseId))

        // 4. Act: Нажимаем на иконку удаления
        val deleteContentDescription = context.getString(ru.pricklycactus.workoutdiary.feature.common.R.string.exercise_delete_content_description)
        composeTestRule.onNodeWithContentDescription(deleteContentDescription).performClick()

        // Assert: Проверяем, что ушел Intent на удаление
        verify { mockStore.dispatch(EditorIntent.OnExercisesDelete(setOf(exerciseId))) }
    }


    private fun setupScreen() {
        composeTestRule.setContent {
            MaterialTheme {
                EditorScreen(
                    state = testState.value,
                    store = mockStore
                )
            }
        }
    }
}
