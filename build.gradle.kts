plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.detekt) apply false
}

val detektFormatting = libs.detekt.formatting

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension>("detekt") {
        config.setFrom(files("${project.rootDir}/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        allRules = false
        autoCorrect = true
    }

    // Configure reports on the tasks directly using the class name to avoid type inference issues
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
