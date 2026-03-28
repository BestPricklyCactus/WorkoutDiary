import com.android.build.api.dsl.CommonExtension

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
    }

    // Настройка всех задач Detekt во всех модулях
    tasks.withType(io.gitlab.arturbosch.detekt.Detekt::class.java).configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
        // Позволяем Jenkins обрабатывать ошибки самостоятельно
        ignoreFailures = true
    }

    dependencies {
        "detektPlugins"(detektFormatting)
    }
}

subprojects {
    apply(plugin = "jacoco")
    configure<org.gradle.testing.jacoco.plugins.JacocoPluginExtension> {
        toolVersion = "0.8.12"
    }
    
    // Используем современный CommonExtension вместо BaseExtension
    pluginManager.withPlugin("com.android.application") {
        extensions.configure<CommonExtension<*, *, *, *, *, *>>("android") {
            buildTypes.getByName("debug") {
                enableUnitTestCoverage = true
            }
        }
    }
    pluginManager.withPlugin("com.android.library") {
        extensions.configure<CommonExtension<*, *, *, *, *, *>>("android") {
            buildTypes.getByName("debug") {
                enableUnitTestCoverage = true
            }
        }
    }
}
