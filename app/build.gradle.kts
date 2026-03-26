import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.util.Properties

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

private val defaultReleaseDname = "CN=WorkoutDiary, OU=Mobile, O=BestPricklyCactus, L=Moscow, ST=Moscow, C=RU"

android {
    namespace = "ru.pricklycactus.workoutdiary"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.pricklycactus.workoutdiary"
        minSdk = 24
        targetSdk = 36
        versionCode = 11
        versionName = "1.1"

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
    category.convention(
        providers.gradleProperty("rustoreCategory")
            .orElse(localProperties.getProperty("rustoreCategory", "health"))
    )

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

val publishToRuStore = tasks.register("publishToRuStore", RuStorePublishTask::class.java) {
    group = "publishing"
    description = "Builds and uploads release APK/AAB to RuStore, then sends it to moderation"

    projectDirPath.set(project.projectDir.absolutePath)
    packageName.set(rustorePublishing.packageName)
    artifactType.set(rustorePublishing.artifactType)
    keyId.set(rustorePublishing.keyId)
    privateKey.set(rustorePublishing.privateKey)
    appType.set(rustorePublishing.appType)
    category.set(rustorePublishing.category)
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
