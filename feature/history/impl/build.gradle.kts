plugins {
    alias(libs.plugins.android.library)
    // alias(libs.plugins.kotlin.android) - Removed to fix double applying error
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "ru.pricklycactus.workoutdiary.feature.history.impl"
    compileSdk = 36
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":core:mvi"))
    implementation(project(":feature:history:api"))
    implementation(project(":feature:common"))
    implementation(project(":data"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.coroutines.core)
}
