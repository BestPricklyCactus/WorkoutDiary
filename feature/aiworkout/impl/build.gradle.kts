import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

val llmApiKey = localProperties.getProperty("llmApiKey", localProperties.getProperty("openAiApiKey", ""))
val llmModel = localProperties.getProperty("llmModel", localProperties.getProperty("openAiModel", "meta-llama/llama-3.1-8b-instruct"))

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ru.pricklycactus.workoutdiary.feature.aiworkout.impl"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        buildConfigField("String", "LLM_API_KEY", "\"$llmApiKey\"")
        buildConfigField("String", "LLM_MODEL", "\"$llmModel\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(project(":feature:aiworkout:api"))
    implementation(project(":data"))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.coroutines.core)
}
