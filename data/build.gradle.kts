plugins {
    alias(libs.plugins.android.library)
    // alias(libs.plugins.kotlin.android) - Removed to fix double applying error
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "ru.pricklycactus.workoutdiary.data"
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
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.dagger)
    ksp(libs.dagger.compiler)
}
