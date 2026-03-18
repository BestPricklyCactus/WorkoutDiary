plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
}

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

    buildTypes {
        release {
            isMinifyEnabled = false
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
