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
        allRules = false
        autoCorrect = true
        
        // Настройка отчетов через расширение (рабочий способ для 1.23+)
        reports {
            xml.required.set(true)
            html.required.set(true)
            xml.outputLocation.set(layout.buildDirectory.file("reports/detekt/detekt.xml"))
            html.outputLocation.set(layout.buildDirectory.file("reports/detekt/detekt.html"))
        }
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

    afterEvaluate {
        if (plugins.hasPlugin("com.android.application") ||
            plugins.hasPlugin("com.android.library")) {

            extensions.findByType<com.android.build.gradle.BaseExtension>()?.apply {
                buildTypes {
                    getByName("debug") {
                        enableUnitTestCoverage = true
                    }
                }
            }
        }
    }
}
