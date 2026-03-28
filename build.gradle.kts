plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.detekt)
}

val detektFormatting = libs.detekt.formatting

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension>("detekt") {
        config.setFrom(files("${project.rootDir}/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = true
        ignoreFailures = true // Позволяет сгенерировать отчеты перед тем как уронить билд
    }

    tasks.withType(io.gitlab.arturbosch.detekt.Detekt::class.java).configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    dependencies {
        "detektPlugins"(detektFormatting)
    }
}

// Конфигурация Jacoco (покрытие тестами)
subprojects {
    apply(plugin = "jacoco")

    configure<org.gradle.testing.jacoco.plugins.JacocoPluginExtension> {
        toolVersion = "0.8.12"
    }

    pluginManager.withPlugin("com.android.application") {
        extensions.configure<com.android.build.gradle.AppExtension>("android") {
            buildTypes.getByName("debug") {
                isTestCoverageEnabled = true
            }
        }
    }

    pluginManager.withPlugin("com.android.library") {
        extensions.configure<com.android.build.gradle.LibraryExtension>("android") {
            buildTypes.getByName("debug") {
                isTestCoverageEnabled = true
            }
        }
    }
}
