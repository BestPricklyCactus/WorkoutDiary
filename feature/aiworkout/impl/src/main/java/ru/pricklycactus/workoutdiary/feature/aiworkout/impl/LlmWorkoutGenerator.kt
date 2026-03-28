package ru.pricklycactus.workoutdiary.feature.aiworkout.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.pricklycactus.workoutdiary.feature.aiworkout.api.AiGeneratedExercise
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class LlmWorkoutGenerator(
    private val apiKey: String,
    private val model: String
) {

    suspend fun generateWorkout(prompt: String): GeneratedWorkoutPlan = withContext(Dispatchers.IO) {
        require(apiKey.isNotBlank()) {
            LlmWorkoutMessages.MissingApiKey
        }

        val connection = (
            URL(
                "https://openrouter.ai/api/v1/chat/completions"
            ).openConnection() as HttpURLConnection
            ).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = REQUEST_TIMEOUT_MILLIS
            readTimeout = REQUEST_TIMEOUT_MILLIS
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("HTTP-Referer", "https://github.com/pricklycactus/WorkoutDiary")
            setRequestProperty("X-Title", "WorkoutDiary")
        }

        try {
            val requestBody = JSONObject()
                .put("model", model)
                .put("temperature", 0.7)
                .put(
                    "messages",
                    JSONArray().apply {
                        put(
                            JSONObject()
                                .put("role", "system")
                                .put(
                                    "content",
                                    LlmWorkoutMessages.SystemPrompt
                                )
                        )
                        put(
                            JSONObject()
                                .put("role", "user")
                                .put("content", prompt)
                        )
                    }
                )

            connection.outputStream.bufferedWriter().use { writer ->
                writer.write(requestBody.toString())
            }

            val responseCode = connection.responseCode
            val responseText = readResponse(connection, responseCode)

            if (responseCode !in 200..299) {
                throw IllegalStateException(extractErrorMessage(responseCode, responseText))
            }

            val content = JSONObject(responseText)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            parsePlan(content)
        } finally {
            connection.disconnect()
        }
    }

    private fun parsePlan(content: String): GeneratedWorkoutPlan {
        val normalizedContent = content
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val json = runCatching { JSONObject(normalizedContent) }
            .getOrElse {
                throw IllegalStateException(
                    LlmWorkoutMessages.InvalidResponseFormat
                )
            }
        val exercisesJson = json.optJSONArray("exercises")
            ?: throw IllegalStateException(
                LlmWorkoutMessages.MissingExercisesList
            )

        val exercises = buildList {
            for (index in 0 until exercisesJson.length()) {
                when (val item = exercisesJson.get(index)) {
                    is JSONObject -> {
                        add(
                            AiGeneratedExercise(
                                id = UUID.randomUUID().toString(),
                                name = item.optString("name").trim().ifBlank {
                                    LlmWorkoutMessages.defaultExerciseName(index + 1)
                                },
                                description = item.optString("description").trim(),
                                sets = item.optInt("sets").takeIf { it > 0 } ?: DEFAULT_SETS,
                                reps = item.optString("reps").trim().ifBlank { DEFAULT_REPS }
                            )
                        )
                    }

                    is String -> {
                        add(
                            AiGeneratedExercise(
                                id = UUID.randomUUID().toString(),
                                name = item.trim().ifBlank { LlmWorkoutMessages.defaultExerciseName(index + 1) },
                                description = LlmWorkoutMessages.DefaultDescription,
                                sets = DEFAULT_SETS,
                                reps = DEFAULT_REPS
                            )
                        )
                    }
                }
            }
        }

        if (exercises.isEmpty()) {
            throw IllegalStateException(
                LlmWorkoutMessages.EmptyExercisesList
            )
        }

        return GeneratedWorkoutPlan(
            title = json.optString("title").trim().ifBlank { LlmWorkoutMessages.DefaultTitle },
            exercises = exercises
        )
    }

    private fun readResponse(connection: HttpURLConnection, responseCode: Int): String {
        val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
        return BufferedReader(InputStreamReader(stream)).use { it.readText() }
    }

    private fun extractErrorMessage(responseCode: Int, responseText: String): String {
        val apiMessage = runCatching {
            JSONObject(responseText).getJSONObject("error").optString("message")
        }.getOrNull().orEmpty()

        return when (responseCode) {
            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                LlmWorkoutMessages.InvalidApiKey
            }
            HttpURLConnection.HTTP_PAYMENT_REQUIRED,
            HttpURLConnection.HTTP_FORBIDDEN -> {
                if (apiMessage.isNotBlank()) {
                    LlmWorkoutMessages.AccountAccessErrorPrefix + apiMessage
                } else {
                    LlmWorkoutMessages.AccountAccessError
                }
            }
            HttpURLConnection.HTTP_BAD_REQUEST -> {
                if (apiMessage.isNotBlank()) {
                    LlmWorkoutMessages.BadRequestPrefix + apiMessage
                } else {
                    LlmWorkoutMessages.BadRequest
                }
            }
            else -> apiMessage.ifBlank { LlmWorkoutMessages.GenericError }
        }
    }
}

private object LlmWorkoutMessages {
    const val MissingApiKey = "Не найден ключ OpenRouter. Добавь llmApiKey в local.properties"
    const val InvalidResponseFormat = "OpenRouter вернул ответ в неожиданном формате. Попробуй еще раз или смени модель."
    const val MissingExercisesList = "OpenRouter вернул план без списка exercises. Попробуй еще раз или смени модель."
    const val EmptyExercisesList = "OpenRouter вернул пустой список упражнений. Попробуй еще раз или смени модель."
    const val DefaultDescription = "Описание не указано"
    const val DefaultTitle = "AI план тренировки"
    const val InvalidApiKey = "OpenRouter отклонил ключ. Проверь llmApiKey в local.properties."
    const val AccountAccessErrorPrefix = "OpenRouter недоступен для текущего аккаунта: "
    const val AccountAccessError = "OpenRouter недоступен для текущего аккаунта или модели. Проверь баланс и модель."
    const val BadRequestPrefix = "Ошибка запроса к OpenRouter: "
    const val BadRequest = "Ошибка запроса к OpenRouter. Проверь модель и параметры запроса."
    const val GenericError = "Не удалось получить ответ от OpenRouter"
    const val SystemPrompt = "Ты фитнес-ассистент. Верни только валидный JSON без markdown, без пояснений, без текста до или после JSON. " +
        "Ответ должен быть строго объектом формата {\"title\": string, \"exercises\": [{\"name\": string, \"description\": string, \"sets\": number, \"reps\": string}]}. " +
        "Поле exercises обязательно должно быть массивом объектов, а не массивом строк. " +
        "У каждого упражнения обязательно должны быть все 4 поля: name, description, sets, reps. " +
        "sets должно быть целым числом от 2 до 5. reps должно быть строкой, например \"10-12\" или \"12\". " +
        "Все названия и описания только на русском языке. " +
        "Сгенерируй от 3 до 6 безопасных и реалистичных упражнений по запросу пользователя. " +
        "Не используй английские слова, если есть русский аналог. " +
        "Пример корректного формата: {\"title\":\"Тренировка на ягодицы\",\"exercises\":[{\"name\":\"Приседания\",\"description\":\"Держи спину ровно и напрягай ягодицы в верхней точке\",\"sets\":4,\"reps\":\"12\"}]}."

    fun defaultExerciseName(index: Int): String = "Упражнение $index"
}

private const val REQUEST_TIMEOUT_MILLIS = 30_000
private const val DEFAULT_SETS = 3
private const val DEFAULT_REPS = "10-12"

data class GeneratedWorkoutPlan(
    val title: String,
    val exercises: List<AiGeneratedExercise>
)
