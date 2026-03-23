import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Properties
import java.util.UUID
import javax.inject.Inject

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

val releaseStoreFile = providers.gradleProperty("releaseStoreFile")
    .orElse(localProperties.getProperty("releaseStoreFile", ""))
val releaseStorePassword = providers.gradleProperty("releaseStorePassword")
    .orElse(localProperties.getProperty("releaseStorePassword", ""))
val releaseKeyAlias = providers.gradleProperty("releaseKeyAlias")
    .orElse(localProperties.getProperty("releaseKeyAlias", ""))
val releaseKeyPassword = providers.gradleProperty("releaseKeyPassword")
    .orElse(localProperties.getProperty("releaseKeyPassword", ""))

val hasReleaseSigning = releaseStoreFile.isPresent &&
    releaseStoreFile.get().isNotBlank() &&
    releaseStorePassword.isPresent &&
    releaseStorePassword.get().isNotBlank() &&
    releaseKeyAlias.isPresent &&
    releaseKeyAlias.get().isNotBlank() &&
    releaseKeyPassword.isPresent &&
    releaseKeyPassword.get().isNotBlank()

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
}

abstract class PublishToRuStoreTask : DefaultTask() {
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
    abstract val categories: ListProperty<String>

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

    @TaskAction
    fun publish() {
        validateConfig()

        val resolvedArtifactFile = resolveArtifactFile()
        logger.lifecycle("Publishing ${resolvedArtifactFile.name} to RuStore for ${packageName.get()}")

        val publicToken = createPublicToken()
        val versionId = createDraft(publicToken)
        uploadArtifact(publicToken, versionId, resolvedArtifactFile)
        updatePublishSettings(publicToken, versionId)
        sendToModeration(publicToken, versionId)

        logger.lifecycle("RuStore draft $versionId uploaded and sent to moderation")
    }

    private fun validateConfig() {
        if (keyId.get().isBlank()) error("rustoreKeyId is required")
        if (privateKey.get().isBlank()) error("rustorePrivateKey is required")
        if (developerEmail.get().isBlank()) error("rustoreDeveloperEmail is required")
        if (categories.get().isEmpty()) error("At least one RuStore category is required")

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
        val file = project.layout.projectDirectory.file(defaultRelativePath).asFile
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
            "categories" to categories.get(),
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

        val response = requestJson(
            method = "POST",
            url = "$rustoreApiHost/public/v1/application/${encodePath(packageName.get())}/version",
            token = publicToken,
            body = body
        )

        val versionBody = response["body"]
        return when (versionBody) {
            is Number -> versionBody.toLong()
            is Map<*, *> -> (versionBody["versionId"] as Number).toLong()
            else -> error("Unexpected create draft response: $versionBody")
        }
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
            throw GradleException("RuStore API error ${connection.responseCode}: $response")
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
            throw GradleException("RuStore upload error ${connection.responseCode}: $response")
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

    private fun error(message: String): Nothing = throw GradleException(message)
}

private val rustoreApiHost = "https://public-api.rustore.ru"
private val requestTimeoutMillis = 30_000
private val defaultReleaseDname = "CN=WorkoutDiary, OU=Mobile, O=BestPricklyCactus, L=Moscow, ST=Moscow, C=RU"
private val allowedPartialValues = setOf(5, 10, 25, 50, 75, 100)

android {
    namespace = "ru.pricklycactus.workoutdiary"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.pricklycactus.workoutdiary"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile.get())
                storePassword = releaseStorePassword.get()
                keyAlias = releaseKeyAlias.get()
                keyPassword = releaseKeyPassword.get()
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

val rustorePublishing = extensions.create("rustorePublishing", RuStorePublishingExtension::class.java)

rustorePublishing.apply {
    packageName.convention(android.defaultConfig.applicationId)
    artifactType.convention(
        providers.gradleProperty("rustoreArtifactType")
            .map { RuStoreArtifactType.valueOf(it.uppercase()) }
            .orElse(RuStoreArtifactType.AAB)
    )
    keyId.convention(
        providers.gradleProperty("rustoreKeyId")
            .orElse(localProperties.getProperty("rustoreKeyId", ""))
    )
    privateKey.convention(
        providers.gradleProperty("rustorePrivateKey")
            .orElse(localProperties.getProperty("rustorePrivateKey", ""))
    )
    appType.convention(
        providers.gradleProperty("rustoreAppType")
            .orElse(localProperties.getProperty("rustoreAppType", "MAIN"))
    )
    categories.convention(listOf("health", "fitness"))
    minAndroidVersion.convention(
        providers.gradleProperty("rustoreMinAndroidVersion")
            .orElse(localProperties.getProperty("rustoreMinAndroidVersion", "8"))
    )
    developerEmail.convention(
        providers.gradleProperty("rustoreDeveloperEmail")
            .orElse(localProperties.getProperty("rustoreDeveloperEmail", "Masha_9595@mail.ru"))
    )
    developerWebsite.convention(
        providers.gradleProperty("rustoreDeveloperWebsite")
            .orElse(localProperties.getProperty("rustoreDeveloperWebsite", ""))
    )
    developerVkCommunity.convention(
        providers.gradleProperty("rustoreDeveloperVkCommunity")
            .orElse(localProperties.getProperty("rustoreDeveloperVkCommunity", ""))
    )
    publishType.convention(
        providers.gradleProperty("rustorePublishType")
            .map { RuStorePublishType.valueOf(it.uppercase()) }
            .orElse(
                RuStorePublishType.valueOf(
                    localProperties.getProperty("rustorePublishType", "MANUAL").uppercase()
                )
            )
    )
    publishDateTime.convention(
        providers.gradleProperty("rustorePublishDateTime")
            .orElse(localProperties.getProperty("rustorePublishDateTime", ""))
    )
    partialValue.convention(
        providers.gradleProperty("rustorePartialValue")
            .map(String::toInt)
            .orElse(localProperties.getProperty("rustorePartialValue", "100").toInt())
    )
    releaseNotes.convention(
        providers.gradleProperty("rustoreReleaseNotes")
            .orElse(localProperties.getProperty("rustoreReleaseNotes", ""))
    )
    priorityUpdate.convention(
        providers.gradleProperty("rustorePriorityUpdate")
            .map(String::toInt)
            .orElse(localProperties.getProperty("rustorePriorityUpdate", "0").toInt())
    )

    val artifactPath = providers.gradleProperty("rustoreArtifactFile")
        .orElse(localProperties.getProperty("rustoreArtifactFile", ""))

    if (artifactPath.isPresent && artifactPath.get().isNotBlank()) {
        artifactFile.convention(layout.projectDirectory.file(artifactPath.get()))
    }
}

gradle.taskGraph.whenReady {
    val categoriesProperty = providers.gradleProperty("rustoreCategories").orNull
    if (!categoriesProperty.isNullOrBlank()) {
        rustorePublishing.categories.set(
            categoriesProperty.split(",").map { it.trim() }.filter { it.isNotBlank() }
        )
    }
}

val publishToRuStore = tasks.register("publishToRuStore", RuStorePublishTask::class.java) {
    group = "publishing"
    description = "Builds and uploads release APK/AAB to RuStore, then sends it to moderation"

    projectDirPath.set(project.projectDir.absolutePath)
    packageName.set(rustorePublishing.packageName)
    artifactType.set(rustorePublishing.artifactType)
    keyId.set(rustorePublishing.keyId)
    privateKey.set(rustorePublishing.privateKey)
    appType.set(rustorePublishing.appType)
    categories.set(rustorePublishing.categories)
    minAndroidVersion.set(rustorePublishing.minAndroidVersion)
    developerEmail.set(rustorePublishing.developerEmail)
    developerWebsite.set(rustorePublishing.developerWebsite)
    developerVkCommunity.set(rustorePublishing.developerVkCommunity)
    publishType.set(rustorePublishing.publishType)
    publishDateTime.set(rustorePublishing.publishDateTime)
    partialValue.set(rustorePublishing.partialValue)
    releaseNotes.set(rustorePublishing.releaseNotes)
    priorityUpdate.set(rustorePublishing.priorityUpdate)

    if (rustorePublishing.artifactFile.isPresent) {
        artifactFile.set(rustorePublishing.artifactFile)
    }
}

afterEvaluate {
    publishToRuStore.configure {
        dependsOn(
            when (rustorePublishing.artifactType.get()) {
                RuStoreArtifactType.APK -> "assembleRelease"
                RuStoreArtifactType.AAB -> "bundleRelease"
            }
        )
    }
}

dependencies {
    implementation(project(":core:mvi"))
    implementation(project(":data"))
    implementation(project(":feature:common"))
    implementation(project(":feature:main:api"))
    implementation(project(":feature:main:impl"))
    implementation(project(":feature:editor:api"))
    implementation(project(":feature:editor:impl"))
    implementation(project(":feature:history:api"))
    implementation(project(":feature:history:impl"))
    implementation(project(":feature:workout:api"))
    implementation(project(":feature:workout:impl"))
    implementation(project(":feature:aiworkout:api"))
    implementation(project(":feature:aiworkout:impl"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.google.material)

    //Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)

    // Dagger
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
