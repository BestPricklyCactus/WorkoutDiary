import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.UUID

abstract class RuStorePublishTask : DefaultTask() {
    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val artifactType: Property<RuStoreArtifactType>

    @get:Input
    abstract val keyId: Property<String>

    @get:Input
    abstract val privateKey: Property<String>

    @get:Input
    abstract val appType: Property<String>

    @get:Input
    abstract val category: Property<String>

    @get:Input
    abstract val minAndroidVersion: Property<String>

    @get:Input
    abstract val developerEmail: Property<String>

    @get:Optional
    @get:Input
    abstract val developerWebsite: Property<String>

    @get:Optional
    @get:Input
    abstract val developerVkCommunity: Property<String>

    @get:Input
    abstract val publishType: Property<RuStorePublishType>

    @get:Optional
    @get:Input
    abstract val publishDateTime: Property<String>

    @get:Input
    abstract val partialValue: Property<Int>

    @get:Optional
    @get:Input
    abstract val releaseNotes: Property<String>

    @get:Input
    abstract val priorityUpdate: Property<Int>

    @get:Optional
    @get:InputFile
    abstract val artifactFile: RegularFileProperty

    @get:Internal
    abstract val projectDirPath: Property<String>

    @TaskAction
    fun publish() {
        validateConfig()

        val resolvedArtifactFile = resolveArtifactFile()
        logger.lifecycle("Publishing ${resolvedArtifactFile.name} to RuStore for ${packageName.get()}")

        val publicToken = createPublicToken()
        deleteExistingDrafts(publicToken)
        var versionId = createDraft(publicToken)
        try {
            uploadArtifact(publicToken, versionId, resolvedArtifactFile)
        } catch (e: GradleException) {
            val message = e.message.orEmpty()
            if (message.contains("There can be only one main APK file", ignoreCase = true)) {
                logger.lifecycle("RuStore reports existing main artifact in draft $versionId. Recreating draft and retrying upload.")
                deleteDraft(publicToken, versionId)
                versionId = createDraft(publicToken)
                uploadArtifact(publicToken, versionId, resolvedArtifactFile)
            } else {
                throw e
            }
        }
        if (publishType.get() != RuStorePublishType.MANUAL) {
            updatePublishSettings(publicToken, versionId)
        }
        sendToModeration(publicToken, versionId)

        logger.lifecycle("RuStore draft $versionId uploaded and sent to moderation")
    }

    private fun validateConfig() {
        if (keyId.get().isBlank()) error("rustoreKeyId is required")
        if (privateKey.get().isBlank()) error("rustorePrivateKey is required")
        if (developerEmail.get().isBlank()) error("rustoreDeveloperEmail is required")
        if (category.get().isEmpty()) error("At least one RuStore category is required")

        val rollout = partialValue.get()
        if (rollout !in allowedPartialValues) {
            error("RuStore partialValue must be one of $allowedPartialValues")
        }

        if (publishType.get() == RuStorePublishType.DELAYED && publishDateTime.orNull.isNullOrBlank()) {
            error("publishDateTime is required when publishType is DELAYED")
        }
    }

    private fun resolveArtifactFile(): File {
        artifactFile.orNull?.asFile?.let { file ->
            if (file.exists()) return file
        }

        val defaultRelativePath = when (artifactType.get()) {
            RuStoreArtifactType.APK -> "build/outputs/apk/release/app-release.apk"
            RuStoreArtifactType.AAB -> "build/outputs/bundle/release/app-release.aab"
        }
        val file = File(projectDirPath.get(), defaultRelativePath)
        if (!file.exists()) {
            error("Artifact not found: ${file.absolutePath}")
        }
        return file
    }

    private fun createPublicToken(): String {
        val timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val signature = signTimestamp(keyId.get(), privateKey.get(), timestamp)

        val response = requestJson(
            method = "POST",
            url = "$rustoreApiHost/public/auth/",
            body = mapOf(
                "keyId" to keyId.get(),
                "timestamp" to timestamp,
                "signature" to signature
            )
        )

        return ((response["body"] as? Map<*, *>)?.get("jwe") as? String)
            ?: error("RuStore auth response does not contain jwe")
    }

    private fun createDraft(publicToken: String): Long {
        val developerContacts = linkedMapOf<String, Any>(
            "email" to developerEmail.get()
        ).apply {
            developerWebsite.orNull?.takeIf { it.isNotBlank() }?.let { put("website", it) }
            developerVkCommunity.orNull?.takeIf { it.isNotBlank() }?.let { put("vkCommunity", it) }
        }

        val body = linkedMapOf<String, Any>(
            "appType" to appType.get(),
            "category" to category.get(),
            "minAndroidVersion" to minAndroidVersion.get(),
            "developerContacts" to developerContacts,
            "publishType" to publishType.get().name,
            "partialValue" to partialValue.get()
        ).apply {
            releaseNotes.orNull?.takeIf { it.isNotBlank() }?.let { put("whatsNew", it) }
            if (publishType.get() == RuStorePublishType.DELAYED) {
                put("publishDateTime", publishDateTime.get())
            }
        }

        return try {
            val response = requestJson(
                method = "POST",
                url = "$rustoreApiHost/public/v1/application/${encodePath(packageName.get())}/version",
                token = publicToken,
                body = body
            )

            val versionBody = response["body"]
            when (versionBody) {
                is Number -> versionBody.toLong()
                is Map<*, *> -> (versionBody["versionId"] as Number).toLong()
                else -> error("Unexpected create draft response: $versionBody")
            }
        } catch (e: Exception) {
            val message = e.message ?: ""
            if (message.contains("You already have draft version with ID =")) {
                val regex = "ID = (\\d+)".toRegex()
                val match = regex.find(message)
                if (match != null) {
                    val existingId = match.groupValues[1].toLong()
                    logger.lifecycle("Draft already exists with ID $existingId. Removing it and creating a new one.")

                    // Удаление существующего черновика
                    requestJson(
                        method = "DELETE",
                        url = "$rustoreApiHost/public/v1/application/${encodePath(packageName.get())}/version/$existingId",
                        token = publicToken
                    )

                    // Создание нового черновика
                    val newResponse = requestJson(
                        method = "POST",
                        url = "$rustoreApiHost/public/v1/application/${encodePath(packageName.get())}/version",
                        token = publicToken,
                        body = body
                    )

                    val newVersionBody = newResponse["body"]
                    when (newVersionBody) {
                        is Number -> newVersionBody.toLong()
                        is Map<*, *> -> (newVersionBody["versionId"] as Number).toLong()
                        else -> error("Unexpected create draft response: $newVersionBody")
                    }
                } else {
                    throw e
                }
            } else {
                throw e
            }
        }
    }

    private fun deleteExistingDrafts(publicToken: String) {
        val response = requestJson(
            method = "GET",
            url = "$rustoreApiHost/public/v1/application/${encodePath(packageName.get())}/version?versionStatuses=DRAFT&page=0&size=100",
            token = publicToken
        )

        val body = response["body"] as? Map<*, *> ?: return
        val content = body["content"] as? List<*> ?: return
        content.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            (map["versionId"] as? Number)?.toLong()
        }.forEach { draftId ->
            logger.lifecycle("Deleting existing RuStore draft $draftId before new publish")
            deleteDraft(publicToken, draftId)
        }
    }

    private fun deleteDraft(publicToken: String, versionId: Long) {
        requestJson(
            method = "DELETE",
            url = "$rustoreApiHost/public/v1/application/${encodePath(packageName.get())}/version/$versionId",
            token = publicToken
        )
    }

    private fun uploadArtifact(publicToken: String, versionId: Long, file: File) {
        val endpoint = when (artifactType.get()) {
            RuStoreArtifactType.APK -> "apk"
            RuStoreArtifactType.AAB -> "aab"
        }

        requestMultipart(
            url = "$rustoreApiHost/public/v1/application/${encodePath(packageName.get())}/version/$versionId/$endpoint",
            token = publicToken,
            file = file
        )
    }

    private fun updatePublishSettings(publicToken: String, versionId: Long) {
        val body = linkedMapOf<String, Any>(
            "publishType" to publishType.get().name,
            "partialValue" to partialValue.get()
        )

        if (publishType.get() == RuStorePublishType.DELAYED) {
            body["publishDateTime"] = publishDateTime.get()
        }

        requestJson(
            method = "POST",
            url = "$rustoreApiHost/public/v1/application/${encodePath(packageName.get())}/version/$versionId/publish-settings",
            token = publicToken,
            body = body
        )
    }

    private fun sendToModeration(publicToken: String, versionId: Long) {
        requestJson(
            method = "POST",
            url = "$rustoreApiHost/public/v1/application/${encodePath(packageName.get())}/version/$versionId/commit?priorityUpdate=${priorityUpdate.get()}",
            token = publicToken,
            body = emptyMap<String, Any>()
        )
    }

    private fun signTimestamp(keyId: String, privateKeyBase64: String, timestamp: String): String {
        val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64))
        val privateKeyValue = KeyFactory.getInstance("RSA").generatePrivate(keySpec)
        val signature = Signature.getInstance("SHA512withRSA")
        signature.initSign(privateKeyValue)
        signature.update((keyId + timestamp).toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(signature.sign())
    }

    private fun requestJson(
        method: String,
        url: String,
        token: String? = null,
        body: Any? = null
    ): Map<*, *> {
        val connection = openJsonConnection(method, url, token)
        if (body != null) {
            connection.doOutput = true
            connection.outputStream.bufferedWriter(StandardCharsets.UTF_8).use {
                it.write(JsonOutput.toJson(body))
            }
        }

        val response = readResponse(connection)
        if (connection.responseCode !in 200..299) {
            throw GradleException("RuStore API error ${connection.responseCode}: ${decodeResponse(response)}")
        }

        val parsed = JsonSlurper().parseText(response)
        return parsed as? Map<*, *> ?: error("Unexpected RuStore JSON response")
    }

    private fun requestMultipart(
        url: String,
        token: String,
        file: File
    ) {
        val boundary = "----RuStoreBoundary${UUID.randomUUID()}"
        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.connectTimeout = requestTimeoutMillis
        connection.readTimeout = requestTimeoutMillis
        connection.setRequestProperty("Public-Token", token)
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

        connection.outputStream.use { output ->
            output.write("--$boundary\r\n".toByteArray())
            output.write(
                "Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"\r\n".toByteArray()
            )
            output.write("Content-Type: application/octet-stream\r\n\r\n".toByteArray())
            file.inputStream().use { it.copyTo(output) }
            output.write("\r\n--$boundary--\r\n".toByteArray())
        }

        val response = readResponse(connection)
        if (connection.responseCode !in 200..299) {
            throw GradleException("RuStore upload error ${connection.responseCode}: ${decodeResponse(response)}")
        }
    }

    private fun openJsonConnection(method: String, url: String, token: String?): HttpURLConnection {
        return (URI(url).toURL().openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = requestTimeoutMillis
            readTimeout = requestTimeoutMillis
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            if (!token.isNullOrBlank()) {
                setRequestProperty("Public-Token", token)
            }
        }
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
        return BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { it.readText() }
    }

    private fun encodePath(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20")

    private fun decodeResponse(value: String): String {
        return runCatching {
            String(value.toByteArray(Charsets.ISO_8859_1), Charsets.UTF_8)
        }.getOrDefault(value)
    }

    private fun error(message: String): Nothing = throw GradleException(message)
}

private val rustoreApiHost = "https://public-api.rustore.ru"
private val requestTimeoutMillis = 300_000
private val allowedPartialValues = setOf(5, 10, 25, 50, 75, 100)
