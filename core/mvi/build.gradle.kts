plugins {
    alias(libs.plugins.android.library)
    // alias(libs.plugins.kotlin.android) - Removed to fix double applying error
}

android {
    namespace = "ru.pricklycactus.workoutdiary.core.mvi"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    api(libs.kotlinx.coroutines.core)
}
